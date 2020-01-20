package it.polimi.distributedsystems.replica;

public class WaitingWrite<T, U, V, W, Z> {

    private final T first;
    private final U second;
    private final V third;
    private final W fourth;
    private final Z fifth;

    public WaitingWrite(T first, U second, V third, W fourth, Z fifth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
        this.fifth = fifth;
    }

    public T getFirst() { return first; }
    public U getSecond() { return second; }
    public V getThird() { return third; }
    public W getFourth() {return fourth; }
    public Z getFifth() { return fifth; }

    @Override
    public String toString() {
        return "Replica N°" + fifth +
                "asked for " + fourth +
                "(" + first +
                ", " + second +
                ")";
    }
}