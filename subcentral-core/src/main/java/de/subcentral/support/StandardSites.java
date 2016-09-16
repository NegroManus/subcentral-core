package de.subcentral.support;

import de.subcentral.core.metadata.Site;
import de.subcentral.support.addic7edcom.Addic7edCom;
import de.subcentral.support.italiansubsnet.ItalianSubsNet;
import de.subcentral.support.orlydbcom.OrlyDbCom;
import de.subcentral.support.predbme.PreDbMe;
import de.subcentral.support.subcentralde.SubCentralDe;
import de.subcentral.support.thetvdbcom.TheTvDbCom;
import de.subcentral.support.xrelto.XRelTo;

public class StandardSites {
	public static final Site	ADDIC7ED_COM	= Addic7edCom.getSite();
	public static final Site	IMDB_COM		= new Site("imdb.com", "IMDb.com", "http://www.imdb.com/");
	public static final Site	ITALIANSUBS_NET	= ItalianSubsNet.getSite();
	public static final Site	ORLYDB_COM		= OrlyDbCom.getSite();
	public static final Site	PREDB_ME		= PreDbMe.getSite();
	public static final Site	SUBCENTRAL_DE	= SubCentralDe.getSite();
	public static final Site	SUBHD_COM		= new Site("subhd.com", "SubHD.com", "http://subhd.com/");
	public static final Site	THETVDB_COM		= TheTvDbCom.getSite();
	public static final Site	THEMOVIEDB_ORG	= new Site("themoviedb.org", "themoviedb.org", "https://www.themoviedb.org/");
	public static final Site	TV4USER_DE		= new Site("tv4user.de", "TV4User.de", "http://board.tv4user.de/");
	public static final Site	XREL_TO			= XRelTo.getSite();
	public static final Site	ZAP2IT_COM		= new Site("zap2it.com", "zap2it.com", "http://zap2it.com/");

	private StandardSites() {
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
