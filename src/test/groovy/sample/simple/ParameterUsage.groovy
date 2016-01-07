package sample.simple

class Summer {
    // explicit arguments and a default value
    def sumWithDefaults(a, b, c = 0) {
        return a + b + c
    }

    // define arguments as a list
    def sumWithList(List args) {
        return args.inject(0) { sum, i -> sum += i }
    }

    // optional arguments as an array
    def sumWithOptionals(a, b, Object[] optionals) {
        return a + b + sumWithList(optionals.toList())
    }

    // define arguments as a map
    def sumNamed(Map args) {
        ['a', 'b', 'c'].each { args.get(it, 0) }
        return args.a + args.b + args.c
    }
}

def summer = new Summer()

assert summer.sumWithDefaults(1, 1) == 2
assert summer.sumWithDefaults(1, 1, 1) == 3

assert summer.sumWithList([1, 1]) == 2
assert summer.sumWithList([1, 1, 1]) == 3
assert summer.sumWithOptionals(1, 1) == 2
assert summer.sumWithOptionals(1, 1, 1) == 3
assert summer.sumWithOptionals(1, 1, 1, 1, 1) == 5

assert summer.sumNamed(a: 1, b: 1) == 2
assert summer.sumNamed(a: 1, b: 1, c: 1) == 3
assert summer.sumNamed(c: 1) == 1

















