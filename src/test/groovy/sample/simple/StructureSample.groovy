package sample.simple

String nullStr = null
if (!nullStr) {
    println("nullStr is empty.")
}

String emptyStr = ''
if (!emptyStr) {
    println("emptyStr is empty.")
}

localInt = 0
//int value cannot be used for boolean judgement
if (localInt) {
    println("this message can never be printed.")
}

for (number in 0..9) {
    print(number)
}
println()

def list = [9, 8, 7, 6, 5]
for (e in list) {
    print(e)
}
println()
list.each { item -> print(item) }
println()


def map = [a: [b: [c: 1]]]
assert map.a.b.c == 1

if (map && map.a && map.a.x) {
    assert map.a.x.c == null
}

try {
    assert map.a.x.c == null
} catch (NullPointerException e) {

}

assert map?.a?.x?.c == null

// calling constructors with positional parameters
class VendorWithCtor {
    String name, product

    VendorWithCtor(name, product) {
        this.name = name
        this.product = product
    }
}

def first = new VendorWithCtor('Canoo', 'ULC')
def second = ['Canoo', 'ULC'] as VendorWithCtor
VendorWithCtor third = ['Canoo', 'ULC']

// calling constructors with named parameters
class Vendor {
    String name, product
}

new Vendor()
new Vendor(name: 'Canoo')
new Vendor(product: 'ULC')
new Vendor(name: 'Canoo', product: 'ULC')
def vendor = new Vendor(name: 'Canoo')
assert 'Canoo' == vendor.name
