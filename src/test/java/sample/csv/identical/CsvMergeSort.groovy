package sample.csv.identical

import au.com.bytecode.opencsv.CSVParser
import com.google.common.base.Preconditions
import com.google.common.base.Splitter
import com.google.common.collect.ComparisonChain
import com.google.common.collect.Iterators
import com.google.common.collect.PeekingIterator
import com.google.common.io.Files
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

import java.nio.charset.Charset

public abstract class CsvMergeSort {
    protected final Collection<File> files
    protected final File result
    protected final Comparator<String> comparator
    protected final long inMemoryLimit
    protected final String lineEnd
    protected final Charset charset
    protected final char separator
    protected final char quote
    protected final char escape
    protected final static long DEFAULT_SIZE_LIMIT = 10L * FileUtils.ONE_MB
    protected final PathMatchingResourcePatternResolver resolver


    protected CsvMergeSort(Collection<File> files,
                           File result = File.createTempFile("sort", "result.csv"),
                           Comparator<String> comparator = { String left, String right -> left.compareTo(right) } as Comparator<String>,
                           long inMemoryLimit = DEFAULT_SIZE_LIMIT,
                           String lineEnd = IOUtils.LINE_SEPARATOR,
                           Charset charset = Charset.defaultCharset(),
                           char separator = CSVParser.DEFAULT_SEPARATOR,
                           char quote = CSVParser.DEFAULT_QUOTE_CHARACTER,
                           char escape = CSVParser.DEFAULT_ESCAPE_CHARACTER) {
        this.files = files
        this.result = result
        this.comparator = comparator
        this.inMemoryLimit = inMemoryLimit
        this.lineEnd = lineEnd
        this.charset = charset
        this.separator = separator
        this.quote = quote
        this.escape = escape
        this.resolver = new PathMatchingResourcePatternResolver()
    }

    public static CsvMergeSort sort(Collection<File> files) {
        Preconditions.checkNotNull(files)
        Preconditions.checkArgument(files.every {
            it != null && it.exists() && it.isFile() && it.canRead()
        }, "%s contains invalid files.", "input path")
        return new CsvMergeSort(files) {}
    }

    public CsvMergeSort withLineEnd(String lineEnd) {
        return new CsvMergeSort(files, result, comparator, inMemoryLimit, lineEnd, charset, separator, quote, escape) {}
    }

    public CsvMergeSort withCharset(Charset charset) {
        return new CsvMergeSort(files, result, comparator, inMemoryLimit, lineEnd, charset, separator, quote, escape) {}
    }

    public CsvMergeSort separator(char separator) {
        return new CsvMergeSort(files, result, comparator, inMemoryLimit, lineEnd, charset, separator, quote, escape) {}
    }

    public CsvMergeSort quote(char quote) {
        return new CsvMergeSort(files, result, comparator, inMemoryLimit, lineEnd, charset, separator, quote, escape) {}
    }

    public CsvMergeSort escape(char escape) {
        return new CsvMergeSort(files, result, comparator, inMemoryLimit, lineEnd, charset, separator, quote, escape) {}
    }


    public CsvMergeSort withInMemorySortSize(long inMemoryLimit) {
        Preconditions.checkArgument(inMemoryLimit > DEFAULT_SIZE_LIMIT, "%s must be greater than default size limit.", "sizeLimit");
        return new CsvMergeSort(files, result, comparator, inMemoryLimit, lineEnd, charset, separator, quote, escape) {}
    }

    public CsvMergeSort by(Comparator<String> comparator) {
        Preconditions.checkNotNull(comparator)
        return new CsvMergeSort(files, result, comparator, inMemoryLimit, lineEnd, charset, separator, quote, escape) {}
    }

    /**
     *
     * @param columnNameTypePairs the key-value pairs which are used to specify to sort which columns and sort by which types.
     * For example, "AdviserNumber=Integer,FirstName=String,LastName=String" means to compare the AdviserNumber column as number, then FirstName column as String,
     * then LastName column as String.
     */
    public CsvMergeSort by(String columnNameTypePairs) {
        Preconditions.checkNotNull(columnNameTypePairs)
        return new CsvMergeSort(files, result, buildComparator(this.files, columnNameTypePairs), inMemoryLimit, lineEnd, charset, separator, quote, escape) {}
    }

    protected Comparator<String> buildComparator(Collection<File> files, String columnNameTypePairs) {
        CSVParser csvParser = new CSVParser(separator, quote, escape)
        String[] headerColumns = csvParser.parseLine(files.first().withReader { it.readLine() })
        def columnNameTypeMap = Splitter.on(',')
                .trimResults()
                .omitEmptyStrings()
                .withKeyValueSeparator("=")
                .split(columnNameTypePairs)
                .collectEntries { columnName, sortType ->
            [headerColumns.findIndexOf {
                it == columnName
            }, sortType]
        }.findAll { it.key != -1 } as LinkedHashMap<Integer, String>
        buildComparator(columnNameTypeMap)
    }

    protected Comparator<String> buildComparator(LinkedHashMap<Integer, String> columnNameTypeMap) {
        CSVParser csvParser = new CSVParser(separator, quote, escape)
        return { String leftLine, String rightLine ->
            String[] left = csvParser.parseLine(leftLine)
            String[] right = csvParser.parseLine(rightLine)
            if (columnNameTypeMap.size() && columnNameTypeMap.find { it.key != -1 }) {
                ComparisonChain chain = ComparisonChain.start()
                columnNameTypeMap.each { index, type ->
                    if (index != -1) {
                        if (type.equalsIgnoreCase('String')) {
                            chain = chain.compare(left[index], right[index])
                        }
                        if (type.equalsIgnoreCase('Integer')) {
                            chain = chain.compare(Integer.parseInt(left[index]), Integer.parseInt(right[index]))
                        }
                    }
                }
                return chain.result()
            } else {
                return leftLine.compareTo(rightLine)
            }
        } as Comparator<String>
    }


    protected static long lineNumber(File input) {
        long lineNumber = 0L;
        input.withReader {
            while (it.readLine()) {
                lineNumber++
            }
        }
        return lineNumber;
    }

    public File result(File file) {
        this.clean()
        String header = this.fetchHeader(files)
        this.combineCsvFiles(files, file, header)
        if (file.length() <= this.inMemoryLimit) {
            this.inMemorySort(file, comparator)
        } else {
            this.mergeSort(file, comparator)
        }
        return file
    }

    public File resultTo(String filePath) {
        Preconditions.checkNotNull(filePath)
        Preconditions.checkArgument(new File(filePath).canWrite(), "%s not exists", filePath)
        return result(new File(filePath))
    }

    public File resultTo(File file) {
        Preconditions.checkNotNull(file)
        Preconditions.checkArgument(file.canWrite(), "%s not exists", file)
        return result(file)
    }

    protected void clean() {
        resolver.getResources("file:/${FileUtils.tempDirectory.canonicalPath}/sort*.csv").each { it.file.delete() }
    }

    protected String fetchHeader(Collection<File> inputFiles) {
        return inputFiles.first().withReader { return it.readLine() }
    }

    protected void combineCsvFiles(Collection<File> from, File to, String header) {
        to.withWriterAppend { writer ->
            writer.write(header.concat(lineEnd))
            from.each { file ->
                file.withReader { reader ->
                    Preconditions.checkState(header == reader.readLine(), "csv file %s contains different headers.", file.canonicalPath)
                    IOUtils.copy(reader, writer)
                }
            }
        }
    }


    protected File inMemorySort(File file, Comparator<String> comparator) {
        String header
        List<String> lines = []
        file.withReader { reader ->
            header = reader.readLine()
            String line
            while (line = reader.readLine()) {
                lines.add(line)
            }
        }
        Collections.sort(lines, comparator);
        file.withWriter { writer ->
            writer.write(header.concat(lineEnd))
            lines.each { writer.write(it.concat(lineEnd)) }
        }
        return file;
    }

    protected File split(File input, long from, long to, String header) {
        File part = File.createTempFile("sort", "part.csv")
        long currentLineNumber = 0L;
        String line
        part.withWriter { writer ->
            if (header) {
                writer.write(header.concat(lineEnd))
            }
            input.withReader { reader ->
                while ((line = reader.readLine()) && currentLineNumber <= to) {
                    if (currentLineNumber >= from) {
                        writer.write(line.concat(lineEnd))
                    }
                    currentLineNumber++
                }
            }
        }
        return part
    }

    protected File merge(File file, String header, File left, File right, Comparator<String> comparator) {
        BufferedWriter writer = Files.newWriter(file, charset)
        BufferedReader leftReader = Files.newReader(left, charset)
        BufferedReader rightReader = Files.newReader(right, charset)
        try {
            PeekingIterator<String> leftPeekingIterator = Iterators.peekingIterator(leftReader.iterator())
            PeekingIterator<String> rightPeekingIterator = Iterators.peekingIterator(rightReader.iterator())
            String leftRecord, rightRecord
            writer.write(header.concat(lineEnd))
            Preconditions.checkState(header == leftPeekingIterator.next(), "%s contains wrong header", left.name)
            Preconditions.checkState(header == rightPeekingIterator.next(), "%s contains wrong header", right.name)
            while (leftPeekingIterator.hasNext() && rightPeekingIterator.hasNext()) {
                leftRecord = leftPeekingIterator.peek()
                rightRecord = rightPeekingIterator.peek()
                if (comparator.compare(leftRecord, rightRecord) < 0) {
                    writer.write(leftRecord.concat(lineEnd))
                    leftPeekingIterator.next()
                } else {
                    writer.write(rightRecord.concat(lineEnd))
                    rightPeekingIterator.next()
                }
            }
            while (leftPeekingIterator.hasNext()) {
                writer.write(leftPeekingIterator.next().concat(lineEnd))
            }
            while (rightPeekingIterator.hasNext()) {
                writer.write(rightPeekingIterator.next().concat(lineEnd))
            }
        } finally {
            writer.close()
            leftReader.close()
            rightReader.close()
        }
        return file
    }

    protected File mergeSort(File file, Comparator<String> comparator) throws IOException {
        if (file.length() <= inMemoryLimit) {
            return inMemorySort(file, comparator)
        }
        final long total = lineNumber(file)
        final long half = lineNumber(file) / 2L as long
        final String header = file.withReader { it.readLine() }
        File left = mergeSort(split(file, 0, half, header), comparator)
        File right = mergeSort(split(file, half + 1L, total, ""), comparator)
        return merge(file, header, left, right, comparator);
    }
}