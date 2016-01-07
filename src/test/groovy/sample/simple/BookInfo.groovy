package sample.simple

String getTitleBackWards(Book book) {
    title = book.getTitle()
    return title.reverse()
}

Book gina = new Book("Groovy in Action")
assert gina.getTitle() == "Groovy in Action"
assert getTitleBackWards(gina) == "noitcA ni yvoorG"


def groovyBook = new Book()

groovyBook.setTitle("Groovy conquers the world")
assert groovyBook.getTitle() == "Groovy conquers the world"

groovyBook.title = "Groovy in Action"
assert groovyBook.title == "Groovy in Action"