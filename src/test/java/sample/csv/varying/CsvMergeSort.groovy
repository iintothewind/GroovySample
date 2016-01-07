package sample.csv.varying

import au.com.bytecode.opencsv.CSV
import au.com.bytecode.opencsv.CSVParser
import au.com.bytecode.opencsv.CSVWriteProc
import au.com.bytecode.opencsv.CSVWriter
import com.google.common.base.Preconditions
import com.google.common.base.Splitter
import com.google.common.collect.ComparisonChain
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

import java.nio.charset.Charset

public abstract class CsvMergeSort {
    protected final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver()
    protected final Collection<File> files
    protected CSV csv
    protected CSVParser csvParser
    protected LinkedHashSet<String> header = new LinkedHashSet<>()
    protected File result
    protected String columnNameTypePairs
    protected Comparator<String[]> comparator
    protected String lineEnd = IOUtils.LINE_SEPARATOR
    protected Charset charset = Charset.defaultCharset()
    protected char separator = CSVParser.DEFAULT_SEPARATOR
    protected char quote = CSVParser.DEFAULT_QUOTE_CHARACTER
    protected char escape = CSVParser.DEFAULT_ESCAPE_CHARACTER


    protected CsvMergeSort(Collection<File> files) {
        this.files = files
        result = File.createTempFile("sort", "result.csv")
        comparator = { String[] left, String[] right -> left.join("").compareTo(right.join("")) } as Comparator<String[]>
    }

    public CsvMergeSort lineEnd(String lineEnd) {
        this.lineEnd = lineEnd
        return this
    }

    public CsvMergeSort charset(Charset charset) {
        this.charset = charset
        return this
    }

    public CsvMergeSort separator(char separator) {
        this.separator = separator
        return this
    }

    public CsvMergeSort quote(char quote) {
        this.quote = quote
        return this
    }

    public CsvMergeSort escape(char escape) {
        this.escape = escape
        return this
    }

    public CsvMergeSort by(Comparator<String[]> comparator) {
        Preconditions.checkNotNull(comparator)
        this.comparator = comparator
        return this
    }

    /**
     *
     * @param columnNameTypePairs the key-value pairs which are used to specify to sort which columns and sort by which types.
     * For example, "AdviserNumber=Integer,FirstName=String,LastName=String" means to compare the AdviserNumber column as number, then FirstName column as String,
     * then LastName column as String.
     */
    public CsvMergeSort by(String columnNameTypePairs) {
        Preconditions.checkNotNull(columnNameTypePairs)
        this.columnNameTypePairs = columnNameTypePairs
        return this
    }

    public static CsvMergeSort sort(Collection<File> files) {
        Preconditions.checkNotNull(files)
        Preconditions.checkArgument(files.every {
            it != null && it.exists() && it.isFile() && it.canRead()
        }, "%s contains invalid files.", "input path")
        return new CsvMergeSort(files) {}
    }

    protected Comparator<String[]> buildComparator(LinkedHashMap<Integer, String> columnIndexTypeMap) {
        return { String[] left, String[] right ->
            if (columnIndexTypeMap.size() && columnIndexTypeMap.find { it.key != -1 }) {
                ComparisonChain chain = ComparisonChain.start()
                columnIndexTypeMap.each { index, type ->
                    if (index != -1) {
                        if (type.equalsIgnoreCase('String')) {
                            chain = chain.compare(left[index], right[index])
                        }
                        if (type.equalsIgnoreCase('Integer')) {
                            chain = chain.compare(Integer.parseInt(left[index] ? left[index] : String.valueOf(Integer.MAX_VALUE)),
                                    Integer.parseInt(right[index] ? right[index] : String.valueOf(Integer.MAX_VALUE)))
                        }
                    }
                }
                return chain.result()
            } else {
                return 0
            }
        } as Comparator<String[]>
    }

    protected Comparator<String[]> buildComparator(LinkedHashSet<String> headerColumns, String columnNameTypePairs) {
        def columnIndexTypeMap = Splitter.on(',')
                .trimResults()
                .omitEmptyStrings()
                .withKeyValueSeparator("=")
                .split(columnNameTypePairs)
                .collectEntries { columnName, sortType ->
            [headerColumns.findIndexOf {
                it == columnName
            }, sortType]
        }.findAll { it.key != -1 } as LinkedHashMap<Integer, String>
        buildComparator(columnIndexTypeMap)
    }

    public File result() {
        clean()
        csvParser = new CSVParser(separator, quote, escape)
        csv = CSV.charset(charset).lineEnd(lineEnd).separator(separator).quote(quote).escape(escape).skipLines(1).create()
        header = sortHeaderColumns(fetchHeader(files))
        comparator = columnNameTypePairs ? buildComparator(header, columnNameTypePairs) : comparator
        return inMemorySort(combineCsvFiles(files), result, comparator)
    }

    public File resultTo(String filePath) {
        Preconditions.checkNotNull(filePath)
        result = new File(filePath)
        return result()
    }

    public File resultTo(File file) {
        Preconditions.checkNotNull(file)
        result = file
        return result()
    }

    protected void clean() {
        resolver.getResources("file:/${FileUtils.tempDirectory.canonicalPath}/sort*result.csv").each { it.file.delete() }
        FileUtils.deleteQuietly(result)
    }

    protected LinkedHashSet<String> fetchHeader(Collection<File> inputFiles) {
        LinkedHashSet<String> combinedHeader = new LinkedHashSet<>()
        Set<String> headerLines = []
        inputFiles.each { headerLines.add(it.withReader { it.readLine() }) }
        Preconditions.checkState(headerLines.size() >= 1, "%s contain no headers, all input files are empty", files)
        headerLines.each { headerLine ->
            csvParser.parseLine(headerLine).each { combinedHeader.add(it) }
        }
        return combinedHeader
    }

    protected LinkedHashSet<String> sortHeaderColumns(LinkedHashSet<String> combinedHeader) {
        LinkedHashSet<String> sortedHeader = ['filename', 'contentType']
        if (combinedHeader.containsAll(sortedHeader)) {
            combinedHeader.removeAll(sortedHeader)
            sortedHeader.addAll(combinedHeader.sort())
            return sortedHeader
        } else {
            return new LinkedHashSet<String>(combinedHeader.sort(true))
        }
    }

    protected Map<Integer, String> calculateIndexColumnNameMap(LinkedHashSet<String> combinedHeader, String[] csvHeaderColumns) {
        Map<Integer, String> indexColumnNameMap = new HashMap<>(csvHeaderColumns.length)
        combinedHeader.eachWithIndex { String entry, int index ->
            if (entry in csvHeaderColumns) {
                indexColumnNameMap.put(index, entry)
            }
        }
        return indexColumnNameMap
    }

    protected String[] generateRow(String[] blankRow, Map<Integer, String> indexColumnNameMap, Map<String, String> rowMap) {
        String[] generatedRow = Arrays.copyOf(blankRow, blankRow.length)
        indexColumnNameMap.each { int indexInCombinedHeader, columnName ->
            generatedRow[indexInCombinedHeader] = rowMap[columnName]
        }
        return generatedRow
    }

    protected List<String[]> combineCsvFiles(Collection<File> inputFiles) {
        List<String[]> combinedRows = []
        String[] blankRow = header.collect { "" }.toArray(new String[0])
        inputFiles.each { file ->
            file.withReader { reader ->
                String line
                String[] csvHeaderColumns
                csvHeaderColumns = csvParser.parseLine(reader.readLine())
                Map<Integer, String> indexColumnNameMap = calculateIndexColumnNameMap(header, csvHeaderColumns)
                Map<String, String> rowMap = new HashMap<>(indexColumnNameMap.size())
                while (line = reader.readLine()) {
                    csvParser.parseLine(line).eachWithIndex { String columnValue, int columnIndex -> rowMap.put(csvHeaderColumns[columnIndex], columnValue) }
                    combinedRows.add(generateRow(blankRow, indexColumnNameMap, rowMap))
                }
                return combinedRows
            }
        }
        return combinedRows
    }

    protected File inMemorySort(List<String[]> rows, File file, Comparator<String[]> comparator) {
        csv.write(file, { CSVWriter csvWriter ->
            csvWriter.writeNext(header as String[])
            rows.sort(true, comparator).each { csvWriter.writeNext(it) }
        } as CSVWriteProc)
        return file
    }
}