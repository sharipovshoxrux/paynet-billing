package uz.baraka.paynetbilling.web.rpc;

import org.springframework.stereotype.Component;

@Component
public class RpcRequestIdHolder {
    private static final ThreadLocal<Object> TL = new InheritableThreadLocal<>();
    public void set(Object id)   { TL.set(id); }
    public Object get()          { return TL.get(); }
    public void clear()          { TL.remove(); }
}
