package sample.csv.varying

import au.com.bytecode.opencsv.CSV
import au.com.bytecode.opencsv.CSVReadProc
import au.com.bytecode.opencsv.CSVWriteProc
import com.google.common.base.Splitter
import com.google.common.collect.ComparisonChain

import org.apache.commons.io.FileUtils
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.springframework.core.io.support.PathMatchingResourcePatternResolver


class CsvMergeSortTest {
    private Collection<File> files
    private File result
    private String sortColumnNameTypePairs = ""
    private LinkedHashSet<String> expectHeaderColumns = new LinkedHashSet<>()
    private PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver()
    private final CSV csv = CSV.create()
    private final int randomLimit = 999
    private int totalDataRowNumber = 0
    private int columnLength = Integer.toString(randomLimit).length()
    private Random random = new Random()


    @Before
    public void setUp() {
        result = File.createTempFile("testResult-", ".csv")
        sortColumnNameTypePairs = ""
        expectHeaderColumns = new LinkedHashSet<>()
        totalDataRowNumber = 0
    }

    @After
    public void tearDown() {
        files.each { printCsv(it) }
        printCsv(result)
        resolver.getResources("file:/${FileUtils.tempDirectory.canonicalPath}/*.csv").each { it.file.delete() }
        files.each { it.delete() }
    }


    private void printCsv(File file) {
        println("${file.canonicalPath} :")
        csv.read(file, { int rowIndex, String[] values ->
            println("${rowIndex}| ${values.collect { it.concat(" " * (columnLength - it.length())) }.join(" ")}")
        } as CSVReadProc)
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

    private File generateCsv(int rowCount, String... columnNames) {
        this.totalDataRowNumber += rowCount
        this.expectHeaderColumns.addAll(columnNames)
        this.expectHeaderColumns = sortHeaderColumns(this.expectHeaderColumns)
        this.columnLength = Math.max(Integer.toString(randomLimit).length(), columnNames.collect { it.length() }.max())
        File tempCsv = File.createTempFile("generated-", ".csv")
        csv.write(tempCsv, { csvWriter ->
            csvWriter.writeNext(columnNames)
            rowCount.times { lineIndex ->
                csvWriter.writeNext(columnNames.collect { Integer.toString(random.nextInt(randomLimit)) } as String[])
            }
        } as CSVWriteProc)
        return tempCsv
    }

    private Comparator<String[]> buildComparator(LinkedHashMap<Integer, String> columnIndexTypeMap) {
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

    private Comparator<String[]> buildComparator(LinkedHashSet<String> headerColumns, String columnNameTypePairs) {
        if (columnNameTypePairs) {
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
        } else {
            { String[] left, String[] right -> left.join("").compareTo(right.join("")) } as Comparator<String[]>
        }
    }

    private void checkSortResult(File result) {
        List<String[]> rows = []
        csv.read(result, { int rowIndex, String[] values ->
            if (rowIndex == 0) {
                // check header
                expectHeaderColumns.eachWithIndex { String headerColumn, int index -> Assert.assertEquals(headerColumn, values[index]) }
            } else {
                rows.add(values)
            }
        } as CSVReadProc)
        // check total row number
        Assert.assertEquals(totalDataRowNumber, rows.size())
        List<String[]> sortedRows = rows.sort(true, buildComparator(expectHeaderColumns, sortColumnNameTypePairs))
        csv.read(result, { int rowIndex, String[] values ->
            if (rowIndex != 0) {
                // check sort result
                sortedRows[rowIndex - 1].eachWithIndex { String columnValue, int columnIndex -> Assert.assertEquals(columnValue, values[columnIndex]) }
            }
        } as CSVReadProc)
    }


    @Test
    public void testTwoFilesHavingSameColumnsWithSameColumnOrder() {
        this.files = [
                this.generateCsv(3, "X", "Y", "Z"),
                this.generateCsv(3, "X", "Y", "Z")
        ]
        sortColumnNameTypePairs = "X=Integer"
        CsvMergeSort.sort(files).by(sortColumnNameTypePairs).resultTo(result)
        checkSortResult(result)
    }

    @Test
    public void testTwoFilesHavingVaryingColumnsSortBySharedColumn() {
        this.files = [
                this.generateCsv(3, "X", "Y", "Z"),
                this.generateCsv(3, "W", "X", "Z")
        ]
        sortColumnNameTypePairs = "X=Integer"
        CsvMergeSort.sort(files).by(sortColumnNameTypePairs).resultTo(result)
        checkSortResult(result)
    }

    @Test
    public void testTwoFilesHavingVaryingColumnsByNonSharedColumn() {
        this.files = [
                this.generateCsv(3, "X", "Y", "Z"),
                this.generateCsv(3, "W", "X", "Z")
        ]
        sortColumnNameTypePairs = "Y=Integer"
        CsvMergeSort.sort(files).by(sortColumnNameTypePairs).resultTo(result)
        checkSortResult(result)
    }

    @Test
    public void testSortColumnNonExisting() {
        this.files = [
                this.generateCsv(3, "X", "Y", "Z"),
                this.generateCsv(3, "W", "X", "Z")
        ]
        sortColumnNameTypePairs = "V=Integer"
        CsvMergeSort.sort(files).by(sortColumnNameTypePairs).resultTo(result)
        checkSortResult(result)
    }

    @Test
    public void testSortColumnsPartiallyExisting() {
        this.files = [
                this.generateCsv(3, "X", "Y", "Z"),
                this.generateCsv(3, "W", "X", "Z")
        ]
        sortColumnNameTypePairs = "V=Integer,W=Integer,Y=Integer"
        CsvMergeSort.sort(files).by(sortColumnNameTypePairs).resultTo(result)
        checkSortResult(result)
    }

    @Test
    public void testTwoFilesHavingSameColumnsWithDiffColumnOrder() {
        this.files = [
                this.generateCsv(3, "X", "Y", "Z"),
                this.generateCsv(3, "Z", "Y", "X")
        ]
        sortColumnNameTypePairs = "V=Integer"
        CsvMergeSort.sort(files).by(sortColumnNameTypePairs).resultTo(result)
        checkSortResult(result)
    }

    @Test
    public void testWithSpecialColumns() {
        this.files = [
                this.generateCsv(3, "X", "Y", "Z", "filename", "contentType"),
                this.generateCsv(3, "Y", "contentType", "Z", "filename", "X")
        ]
        sortColumnNameTypePairs = "filename=String"
        CsvMergeSort.sort(files).by(sortColumnNameTypePairs).resultTo(result)
        checkSortResult(result)
    }

    @Test
    public void testOmitBy() {
        this.files = [
                this.generateCsv(3, "X", "Y", "Z", "filename", "contentType"),
                this.generateCsv(3, "Y", "contentType", "Z", "filename", "X")
        ]
        CsvMergeSort.sort(files).resultTo(result)
        checkSortResult(result)
    }

    @Test
    public void testSortByWrongType() {
        this.files = [
                this.generateCsv(3, "X", "Y", "Z", "filename", "contentType"),
                this.generateCsv(3, "Y", "contentType", "Z", "filename", "X")
        ]
        sortColumnNameTypePairs = "X=WrongType"
        CsvMergeSort.sort(files).by(sortColumnNameTypePairs).resultTo(result)
        checkSortResult(result)
    }
}
