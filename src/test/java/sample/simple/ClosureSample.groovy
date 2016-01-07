package sample.simple

[1, 2, 3].each { println it }
startIndex = 1
endIndex = 3
mySum = (startIndex..endIndex).sum()
println "mySum == " + mySum
def totalClinks = 0
def partyPeople = 100
1.upto(partyPeople) {
    guestNumber ->
        clinksWithGuest = guestNumber - 1
        totalClinks += clinksWithGuest
}
assert totalClinks == (partyPeople * (partyPeople - 1)) / 2

def printer = { line -> println line }

def Closure getPrinter() { return { line -> println line } }

//Simple method closures in action
class MethodClosureSample {
    int limit

    MethodClosureSample(int limit) {
        this.limit = limit
    }

    boolean validate(String value) {
        return value.length() <= limit
    }
}

MethodClosureSample first = new MethodClosureSample(6)
MethodClosureSample second = new MethodClosureSample(5)

Closure firstClosure = first.&validate
def words = ['long string', 'medium', 'short', 'tiny']

assert 'medium' == words.find(firstClosure)
assert 'short' == words.find(second.&validate)

// Multimethod closures
// the same method name called with different parameters is used to call different implementations
class MultimethodSample {
    int mysteryMethod(String value) {
        return value.length()
    }

    int mysteryMethod(List list) {
        return list.size()
    }

    int mysteryMethod(int x, int y) {
        return x + y
    }
}

MultimethodSample instance = new MultimethodSample()
Closure multi = instance.&mysteryMethod

assert 10 == multi('String arg')
assert 3 == multi(['list', 'of', 'values'])
assert 14 == multi(6, 8)

// full closure declaration examples
map = ['a': 1, 'b': 2]
map.each { key, value -> map[key] = value * 2 }
assert map == ['a': 2, 'b': 4]

doubler = { key, value -> map[key] = value * 2 }
map.each(doubler)
assert map == ['a': 4, 'b': 8]

def doubleMethod(entry) {
    map[entry.key] = entry.value * 2
}

doubler = this.&doubleMethod
map.each(doubler)
assert map == ['a': 8, 'b': 16]

// calling closures                                                                                                                                                    curry
def adder = { x, y -> return x + y }
assert adder(4, 3) == 7
assert adder.call(2, 6) == 8

def benchmark(repeat, Closure worker) {
    start = System.currentTimeMillis()
    repeat.times { worker(it) }
    stop = System.currentTimeMillis()
    return stop - start
}

slow = benchmark(10000) { (int) it / 2 }
fast = benchmark(10000) { it.intdiv(2) }
assert fast < slow

// a simple currying example
adder = { x, y -> println('x == ' + x + ', y== ' + y); return x + y }
def addOne = adder.curry(1)
assert addOne(5) == 6

configurator = { format, filter, line -> filter(line) ? format(line) : null }
appender = { config, append, line -> out = config(line); if (out) append(out) }

dateFormatter = { line -> "${new Date()}: $line" }
debugFilter = { line -> line.contains('debug') }
consoleAppender = { line -> println line }

myConf = configurator.curry(dateFormatter, debugFilter)
myLog = appender.curry(myConf, consoleAppender)

myLog('here is some debug message')
myLog('this will not be printed')

// investigating the closure scope
class Mother {
    int field = 1

    int foo() {
        return 2
    }

    Closure birth(param) {
        def local = 3
        def closure = { caller -> [this, field, foo(), local, param, caller] }
        return closure
    }
}

Mother julia = new Mother()
closure = julia.birth(4)
context = closure.call(this)
println context[0].class.name

assert context[1..4] == [1, 2, 3, 4]
assert context[5] instanceof Script
//assert context[6] instanceof Mother

firstClosure = julia.birth(4)
secondClosure = julia.birth(4)
assert false == firstClosure.is(secondClosure)

// the accumulator problem in Groovy
def foo(n) {
    return { n += it }
}

def accumulator = foo(1)
assert accumulator(2) == 3
assert accumulator(1) == 4















