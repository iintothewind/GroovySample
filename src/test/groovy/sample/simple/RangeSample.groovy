package sample.simple

def x = 1..10
assert x.contains(5)
assert x.contains(15) == false
assert x.size() == 10
assert x.from == 1
assert x.to == 10
assert x.contains(x.from)
assert x.contains(x.to)
assert x.reverse() == 10..1
assert x instanceof Range

def a = new IntRange(0, 10)
assert a.contains(4)

// assertion failed, why?
//assert (0.0..1.0).contains(0.5)

def today = new Date()
def yesterday = today - 1
assert (yesterday..today).size() == 2

assert ('a'..'c').contains('b')

log = ''
for (element in 5..9) {
    log += element
}
assert log == '56789'

log = ''
for (element in 9..5) {
    log += element
}
assert log == '98765'

log = ''
(9..<5).each { element -> log += element }
assert log == '9876'




result = ''
(5..9).each { element -> result += element }
assert result == '56789'

assert (0..10).isCase(5)

age = 36
insuranceRate = 0.0f
switch (age) {
    case 16..20: insuranceRate = 0.05; break
    case 21..50: insuranceRate = 0.06; break
    case 51..65: insuranceRate = 0.07; break
    default: throw new IllegalArgumentException()
}
assert insuranceRate == 0.06
ages = [20, 36, 42, 56]
midage = 21..50
assert ages.grep(midage) == [36, 42]


def mon = new Weekday('Mon')
def fri = new Weekday('Fri')
def workLog = ''
for(day in mon..fri) {
    workLog += day.toString() + ' '
}
assert workLog == 'Mon Tue Wed Thu Fri '











































