package sample.simple

def roman = ['', 'I', 'II', 'III', 'IV', 'V', 'VI', 'VII']
assert roman[4] == 'IV'
roman[8] = 'VIII'
roman[10] = 'XI'
assert roman.size() == 11
roman[9] = 'X'
assert roman.size() == 11
assert roman instanceof ArrayList

longList = (0..1000).toList()
assert longList[555] == 555

myList = ['a', 'b', 'c', 'd', 'e', 'f']
assert myList[0..2] == ['a', 'b', 'c']
assert myList[0, 2, 4] == ['a', 'c', 'e']

// putAt(Range)
myList[0..2] = ['x', 'y', 'z']
assert myList == ['x', 'y', 'z', 'd', 'e', 'f']

// removing elements
myList[3..5] = []
assert myList == ['x', 'y', 'z']

// adding elements
myList[1..1] = ['y', '1', '2']
assert myList == ['x', 'y', '1', '2', 'z']

myList = []
myList += 'a'
assert myList == ['a']

myList += ['b', 'c']
assert myList == ['a', 'b', 'c']

myList = []
myList << 'a' << 'b'
assert myList == ['a', 'b']
assert myList - ['b'] == ['a']
assert myList * 2 == ['a', 'b', 'a', 'b']
myList *= 2
assert myList - ['a'] == ['b', 'b']

assert myList.isCase('a')
candidate = 'a'
switch (candidate) {
    case myList: assert true; break
    default: assert false
}

// intersection filter
assert ['x', 'a', 'z'].grep(myList) == ['a']

//Empty lists are false
myList = []
if (myList) assert false

// Lists can be iterated with a 'for' loop
log = ''
for (i in [1, 'x', 5]) {
    log += i
}
assert log == '1x5'


assert [1, [2, 3]].flatten() == [1, 2, 3]
assert [1, 2, 3].intersect([4, 3, 1]) == [3, 1]
assert [1, 2, 3].disjoint([4, 5, 6])


myList = [1, 2, 3]
assert !(4 in myList)
assert !(null in myList)
popped = myList.pop()
assert popped == 3
assert myList == [1, 2]


assert [1, 2].reverse() == [2, 1]
assert [3, 1, 2].sort() == [1, 2, 3]

myList = [[1, 0], [0, 1, 2]]
//comparing lists by first element
myList = myList.sort({ a, b -> a[0] <=> b[0] })
assert myList == [[0, 1, 2], [1, 0]]

myList = myList.sort({ item -> item.size() })
assert myList == [[1, 0], [0, 1, 2]]

myList = ['a', 'b', 'c']
//removing by index
myList.remove(2)
assert myList == ['a', 'b']
//remove by value
myList.remove('b')
assert myList == ['a']

// transform
doubledList = [1, 2, 3].collect { item -> item * 2 }
assert doubledList == [2, 4, 6]

odd = [1, 2, 3].findAll { item -> item % 2 == 1 }
assert odd == [1, 3]



myList = [1, 2, 3]

assert myList.count(2) == 1
assert myList.max() == 3
assert myList.min() == 1

even = myList.find { item -> item % 2 == 0 }
assert even == 2

greater = myList.findAll { item -> item > 0 }
println greater

assert myList.every { item -> item < 5 }
assert myList.any { item -> item < 2 }

store = ''
myList.each { item -> store += item }
assert store == '123'

store = ''
myList.reverseEach { item -> store += item }
assert store == '321'

assert myList.join('-') == '1-2-3'
result = myList.inject(0) { clinks, guests -> clinks += guests }
assert result == 0 + 1 + 2 + 3
assert myList.sum() == 6

factorial = myList.inject(1) { fac, item -> fac *= item }
assert factorial == 1 * 1 * 2 * 3

def quickSort(list) {
    if (list.size() < 2) return list
    def pivot = list[list.size().intdiv(2)]
    def left = list.findAll { item -> item < pivot }
    def middle = list.findAll { item -> item == pivot }
    def right = list.findAll { item -> item > pivot }
    return (quickSort(left) + middle + quickSort(right))
}

assert quickSort([]) == []
assert quickSort([1]) == [1]
assert quickSort([1, 2]) == [1, 2]
assert quickSort([2, 1]) == [1, 2]
assert quickSort([3, 1, 2]) == [1, 2, 3]
assert quickSort([3, 1, 2, 2]) == [1, 2, 2, 3]
assert quickSort([1.0f, 'a', 10, null]) == [null, 1.0f, 10, 'a']
assert quickSort(('Karin and Direrk')) == '  DKaadeiiknnrrr'.toList()



































