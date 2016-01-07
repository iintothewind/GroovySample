package sample.simple

class Weekday implements Comparable<Weekday> {
    static final DAYS = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']
    private int index = 0

    Weekday(String day) {
        index = DAYS.indexOf(day);
    }

    Weekday next() {
        return new Weekday(DAYS[(index + 1) % DAYS.size()])
    }

    Weekday previous() {
        return new Weekday(DAYS[index - 1])
    }


    @Override
    int compareTo(Weekday other) {
        return this.index <=> other.index
    }

    String toString() {
        return DAYS[index]
    }
}
