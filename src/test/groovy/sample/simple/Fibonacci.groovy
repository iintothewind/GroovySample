package sample.simple

def current = 1
def next = 1
def newCurrent = 0
10.times {
    print current + " "
    newCurrent = next
    next = next + current
    current = newCurrent
}

println " "


