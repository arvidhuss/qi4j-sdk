package org.qi4j.test.mock;

import org.qi4j.api.concern.ConcernOf;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MockPlayerConcern
    extends ConcernOf<InvocationHandler>
    implements InvocationHandler
{
    /**
     * @see java.lang.reflect.InvocationHandler#invoke(Object, java.lang.reflect.Method, Object[])
     */
    public Object invoke( final Object proxy, final Method method, final Object[] args )
        throws Throwable
    {
        // TODO implementation
        throw new UnsupportedOperationException( "MockResolver player concern is not yet implemented" );
    }
}
