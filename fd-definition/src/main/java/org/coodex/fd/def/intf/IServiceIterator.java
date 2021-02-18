package org.coodex.fd.def.intf;

public interface IServiceIterator<T extends IProviderSelector> {
    void iterate(T service);
}
