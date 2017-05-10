package org.pratikpharma.disambiguation.ontology.ehealth;


@SuppressWarnings("PublicMethodNotExposedInInterface")
public class AtomicDouble {
    private double value;

    public AtomicDouble(final double value) {
        this.value = value;
    }

    public synchronized double getValue() {
        return value;
    }

    public synchronized void setValue(final double value) {
        this.value = value;
    }

    public synchronized void addValue(final double value) {
        this.value += value;
    }
}
