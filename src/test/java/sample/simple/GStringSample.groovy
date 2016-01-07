package sample.simple


def nick = 'Gina'
def book = 'Groovy in Action'
assert "$nick is $book" == 'Gina is Groovy in Action'

me = 'Tarzan'
you = 'Jane'
println "me $me - you $you"

date = new Date(0)
println "Year $date.year Month $date.month Day $date.date"
println "Date is ${date.toGMTString()}"

greeting = 'Hello Groovy!'
assert greeting.startsWith('Hello')
assert greeting.getAt(0) == 'H'
assert greeting[0] == 'H'
assert greeting[6..11] == 'Groovy'
assert 'Hi' + greeting - 'Hello' == 'Hi Groovy!'
assert greeting.count('o') == 3

assert 'x'.padLeft(3) == '  x'
assert 'x'.padRight(3, '_') == 'x__'
assert 'x'.center(3) == ' x '
assert 'x'.center(3, '-') == '-x-'
assert 'x' * 3 == 'xxx'


greeting = 'Hello'
greeting <<= ' Groovy'
assert greeting instanceof java.lang.StringBuffer
greeting << '!'
assert greeting.toString() == 'Hello Groovy!'
greeting[1..4] = 'i'
assert greeting.toString() == 'Hi Groovy!'
greeting = "*hello*"


println 'SG-Role-EasyDoc-App-Admin-asdfasdf'-'SG-Role-EasyDoc-App-Admin-'

final String fi = "Testing"
assert fi.concat('as') == "Testingas"