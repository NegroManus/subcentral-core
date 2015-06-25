package de.subcentral.support.releasescene;

public class ReleaseSceneParsingPlayground
{

    public static void main(String[] args)
    {
	String name = "Paul.Kalkbrenner.A.Live.Documentary.2010.COMPLETE.MBLURAY-HDA";

	System.out.println(ReleaseScene.getParsingService().parse(name));
    }
}
