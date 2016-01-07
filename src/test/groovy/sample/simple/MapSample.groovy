package sample.simple

def http = [
        100: 'CONTINUE',
        200: 'OK',
        400: 'BAD REQUEST'
]
println "A=" + ("a".toCharacter() - 96)
assert http[200] == 'OK'
http[500] = 'INTERNAL SERVER ERROR'
assert http.size() == 4
assert http.get(600, 'TEST') == 'TEST'
assert http.size() == 5
assert http == [
        100: 'CONTINUE',
        200: 'OK',
        400: 'BAD REQUEST',
        500: 'INTERNAL SERVER ERROR',
        600: 'TEST'
]

def myMap = [a: 1, b: 2, c: 3]
assert myMap instanceof HashMap
assert myMap.size() == 3
assert myMap['a'] == 1

def emptyMap = [:]
assert emptyMap.size() == 0

def explicitMap = new TreeMap()
explicitMap.putAll(myMap)
assert explicitMap['a'] == 1

assert ['a': 1] == [a: 1]
def x = 'a'
assert ['x': 1] == [x: 1]
assert ['a': 1] == [(x): 1]


assert myMap['a'] == 1
assert myMap.a == 1
assert myMap.get('a') == 1
assert myMap.get('z', 26) == 26

assert myMap['d'] == null
assert myMap.d == null
assert myMap.get('d') == null

assert myMap.get('d', 0) == 0
assert myMap.d == 0

myMap['d'] = 1
assert myMap.d == 1
myMap.d = 2
assert myMap.d == 2

// query methods on maps
myMap = [a: 1, b: 2, c: 3]
other = [b: 2, c: 3, a: 1]
assert myMap == other

assert myMap.isEmpty() == false
assert myMap.size() == 3
assert myMap.containsKey('a')

def toSet(list) {
    new java.util.HashSet(list)
}

assert myMap.keySet() == toSet(['a', 'b', 'c'])
assert toSet(myMap.values()) == toSet([1, 2, 3])
assert myMap.entrySet() instanceof Collection

assert myMap.any { entry -> entry.value > 2 }
assert myMap.every { entry -> entry.key < 'd' }

// iterating over maps
myMap = [a: 1, b: 2, c: 3]
store = ''
myMap.each { entry -> store += entry.key; store += entry.value }
assert store == 'a1b2c3'

store = ''
myMap.each { key, value -> store += key; store += value }
assert store == 'a1b2c3'

store = ''
for (key in myMap.keySet()) {
    store += key
}
assert store == 'abc'

store = ''
for (value in myMap.values()) {
    store += value
}
assert store == '123'

//changing map content and building new objects from it
myMap = [a: 1, b: 2, c: 3]
myMap.clear()
assert myMap.isEmpty()

myMap = [a: 1, b: 2, c: 3]
myMap.remove('a')
assert myMap.size() == 2

myMap = [a: 1, b: 2, c: 3]
abMap = myMap.subMap(['a', 'b'])
assert abMap == [a: 1, b: 2]

abMap = myMap.findAll { entry -> entry.value < 3 }
assert abMap == [a: 1, b: 2]

found = myMap.find { entry -> entry.value < 2 }
assert found.key == 'a'
assert found.value == 1

doubled = myMap.collect { entry -> entry.value *= 2 }
assert doubled instanceof List
assert doubled.every { item -> item % 2 == 0 }

addTo = []
myMap.collect(addTo) { entry -> entry.value *= 2 }
assert doubled instanceof List
assert addTo.every { item -> item % 2 == 0 }

//counting word frequency with maps
textCorpus =
        """
Look for the bare necessities
The simple bare necessities
Forget about your worries and your strife
I mean the bare necessities
Old Mother Nature's recipes
Than bring the bare necessities of life
"""


words = textCorpus.tokenize()
wordFrenquency = [:]
words.each { word -> wordFrenquency[word] = wordFrenquency.get(word, 0) + 1 }
wordList = wordFrenquency.keySet().toList()
wordList.sort { wordFrenquency[it] }

statistic = '\n'
wordList[-1..-6].each { word ->
    statistic += word.padLeft(12) + ': '
    statistic += wordFrenquency[word] + "\n"
}

println statistic

assert statistic ==
        """
 necessities: 4
        bare: 4
         the: 3
        your: 2
        life: 1
          of: 1
"""





































































