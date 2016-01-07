package sample.simple

assert true
assert !false

assert ('a' =~ /./)
assert !('a' =~ /b/)

assert [1]
assert ![]

assert ['a': 1]
assert ![:]

assert 'a'
assert !''

assert 1
assert 1.1
assert 1.2f
assert 1.3g
assert 2L
assert 3G
assert !0

assert new Object()
assert !null

def x = 1
if (x == 2) {
    assert false
}

/* compilation error
if(x=2) {
    println x
}
*/

if ((x = 3)) {
    println(x)
}
assert x == 3

def store = []
while (x = x - 1) {
    store << x
}
assert store == [2, 1]

while(x=1) {
    println x
    break
}