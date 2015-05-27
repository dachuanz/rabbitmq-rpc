
package org.apache.qpid.contrib.json.example;


/**
 * @author zdc
 * @since 2015年5月27日
 */
public class TestJsonIm implements TestJson {

    /* (non-Javadoc)
     * @see org.apache.qpid.contrib.json.example.TestJson#getJson()
     */
    @Override
    public int getJson(Integer in) {
        
        return fib(in);
    }
    
    private static int fib(Integer n)  {
        if (n == 0) return 0;
        if (n == 1) return 1;
        return fib(n-1) + fib(n-2);
    }

}
