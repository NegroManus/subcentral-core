package de.subcentral.support.subcentralde;

import java.io.IOException;
import java.nio.file.Path;

public interface SubCentralApi
{
    public abstract void login(String username, String password) throws IOException;

    /**
     * <pre>
     * wcf_userID:	21754
     * wcf_password:	f191d317ad977506c2e71fbbf91097f83f8bd1ca
     * wcf_cookieHash:	6a9df383589c676a9c4c26ba4de65059815d782c
     * wcf_boardLastActivityTime:	1433160906
     * </pre>
     * 
     * @deprecated Currently not working (gets 404 because Cookies userID and password (hash) are missing
     * @throws IOException
     */
    public abstract void logout() throws IOException;

    public abstract void downloadAttachment(int attachmentId, Path directory) throws IOException;

}