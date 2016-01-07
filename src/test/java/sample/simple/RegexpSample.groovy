package sample.simple

assert '12345' =~ /\d+/
assert 'xxxxx' == '12345'.replaceAll(/\d/, 'x')
assert 'ab  c' ==~ /ab\s+c/
assert '123a45adsf>' ==~ /^\d+(\.\d+)?(.*)/
assert '20/09/2014' ==~ /\d\d\/\d\d\/\d\d\d\d/
assert 'a123x56b' ==~ /a(\S+x\S+)?b/

twister = 'she sells sea shells at the sea shore of seychelles'

// twister must contain a substring of size 3
// that starts with s and ends with a
assert twister =~ /s.a/

finder = (twister =~ /s.a/)
assert finder instanceof java.util.regex.Matcher

//twister must contain only words delimited by single spaces
assert twister ==~ /(\w+ \w+)*/

WORD = /\w+/
matches = (twister ==~ /($WORD $WORD)*/)
assert matches instanceof java.lang.Boolean

assert (twister ==~ /s.e/) == false

wordsByX = twister.replaceAll(WORD, 'X')
assert wordsByX == 'X X X X X X X X X X'

words = twister.split(/ /)
assert words.size() == 10
assert words[0] == 'she'


myFairStringy = 'The rain in Spain stays mainly in the plain!'

// words that end with 'ain': \b\w*ain\b
BOUNDS = /\b/
rhyme = /$BOUNDS\w*ain$BOUNDS/
found = ''
myFairStringy.eachMatch(rhyme) {
    match -> found += match + ' '
}
assert found == 'rain Spain plain '

found = ''
(myFairStringy =~ rhyme).each { match -> found += match + ' ' }
assert found == 'rain Spain plain '

cloze = myFairStringy.replaceAll(rhyme) { it - 'ain' + '___' }
assert cloze == 'The r___ in Sp___ stays mainly in the pl___!'

matcher = 'a b c' =~ /\S/
assert matcher[0] == 'a'
assert matcher[1..2] == ['b', 'c']
assert matcher.count == 3

matcher = 'a:1 b:2 c:3' =~ /(\S+):(\S+)/
assert matcher.hasGroup()
assert matcher[0] == ['a:1', 'a', '1']
matcher.eachWithIndex { group, index -> group.eachWithIndex { item, i -> println "group[$index][$i] == " + item } }

('xy' =~ /(.)(.)/).each { all, x, y ->
    assert all == 'xy'
    assert x == 'x'
    assert y == 'y'
}

// some more complicated regex:
// word that starts and ends with same letter
regex = /\b(\w)\w*\1\b/

start = System.currentTimeMillis()
100000.times { twister =~ regex }
first = System.currentTimeMillis() - start

start = System.currentTimeMillis()
pattern = ~regex
100000.times { pattern.matcher(twister) }
second = System.currentTimeMillis() - start

assert first > second * 1.20


assert (~/..../).isCase('bear')
switch ('bear') {
    case ~/..../: assert true; break
    default: assert false
}
beasts = ['bear', 'wolf', 'tiger', 'regex']
assert beasts.grep(~/..../) == ['bear', 'wolf']

assert !"sss".grep(~/[~\/\^\$\+\?\*\-]/)

def config = /admin/
assert !(~/^\*$config+$/).isCase('*sadmins')
assert (~/^\*$config+$/).isCase('*admin')
assert (~/^\*\S+\*$/).isCase('*abc*')
assert (~/^\S+\*$/).isCase('abc*')
assert (~/^\S+\*$/).isCase('*abc*')


assert '*admin'.substring(1) == "admin"
assert 'admin*' - '*' == 'admin'

assert (~/.{2,4}/).isCase('ab')
assert (~/.{2,4}/).isCase('   b')


def pattern = "test.txt"

assert (~/^.*${pattern}.*/).isCase("asdfasdftest.txtasdfasdfasdf")
assert (~/^[\*\s]*${pattern}[\*\s]*/).isCase(" test.txt *")

assert (~/^[^\*]+\*[^\*]+$/).isCase("as*a")

pattern = "find*as"
def index = pattern.indexOf('*')
println pattern.substring(0, index)
println pattern.substring(index + 1, pattern.size())
































