public class NumberTest
{

	/**
	 * Findet alle Quadrate von Primzahlen zwischen 100.000 und 999.999.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		for (int n = 100_000; n < 999_999; n++)
		{
			int singleDivider = -1;
			for (int divider = 2; divider < n; divider++)
			{
				if (n % divider == 0)
				{
					if (singleDivider == -1)
					{
						singleDivider = divider;
					}
					else
					{
						singleDivider = -2;
						break;
					}
				}
			}
			if (singleDivider > 0)
			{
				System.out.println("n=" + n + " -> dividers: 1, " + singleDivider + ", " + n);
			}
		}
	}

}
