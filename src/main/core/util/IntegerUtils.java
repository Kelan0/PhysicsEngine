package main.core.util;

import java.math.RoundingMode;

/**
 * @author Kelan
 */
public class IntegerUtils
{
    public static int divide(int p, int q, RoundingMode mode)
    {
        if (mode == null)
            throw new NullPointerException("Null rounding mode");

        if (q == 0)
            throw new ArithmeticException("div by zero");

        int div = p / q;
        int rem = p - q * div;

        if (rem == 0)
            return div;

        int sign = 1 | ((p ^ q) >> (Integer.SIZE - 1));
        boolean flag;

        switch (mode)
        {
            case UNNECESSARY: // we already checked rem and returned if it was zero. If we reached this, rounding was necessary
                throw new ArithmeticException("Rounding mode was \"UNNECESSARY\" when rounding was needed");
            case DOWN:
                flag = false;
                break;
            case UP:
                flag = true;
                break;
            case CEILING:
                flag = sign > 0;
                break;
            case FLOOR:
                flag = sign < 0;
                break;
            case HALF_EVEN:
            case HALF_DOWN:
            case HALF_UP:
                int absRem = Math.abs(rem);
                int temp = absRem - (Math.abs(q) - absRem);

                if (temp == 0)
                    flag = (mode == RoundingMode.HALF_UP || (mode == RoundingMode.HALF_EVEN && (div & 1) != 0));
                else
                    flag = temp > 0;
                break;
            default:
                throw new AssertionError();
        }
        return flag ? div + sign : div;
    }

    public static int remainder(int p, int q)
    {
        return p % q;
    }

    public static int modulous(int p, int q)
    {
        return remainder(remainder(p, q) + q, q);
    }
}
