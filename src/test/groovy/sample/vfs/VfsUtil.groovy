package sample.vfs

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.commons.vfs2.*
import org.apache.commons.vfs2.impl.StandardFileSystemManager

public class VfsUtil {
    private static Log log = LogFactory.getLog(VfsUtil.class);
    private static StandardFileSystemManager manager;

    private static <T> T withStandardFileSystemManager(Closure<T> closure) {
        manager = new StandardFileSystemManager();
        manager.setLogger(log);
        try {
            manager.init();
            return closure.call(manager);
        } catch (FileSystemException e) {
            throw new RuntimeException(e);
        } finally {
            manager.close();
        }
    }

    public static FileObject resolve(String url, FileSystemOptions options = null) {
        return withStandardFileSystemManager({ manager ->
            try {
                return options ? manager.resolveFile(url, options) : manager.resolveFile(url);
            } catch (FileSystemException e) {
                throw new RuntimeException(e);
            }
        } as Closure<FileObject>);
    }

    public static List<FileObject> list(String url, FileSystemOptions options = null) {
        return withStandardFileSystemManager({ StandardFileSystemManager manager ->
            FileObject fileObject
            try {
                fileObject = options ? manager.resolveFile(url, options) : manager.resolveFile(url);
                return Arrays.asList(fileObject.getChildren());
            } catch (FileSystemException e) {
                throw new RuntimeException(e);
            }
        } as Closure<List<FileObject>>);
    }

    public static FileObject copy(String fromUrl, FileSystemOptions fromOptions = null,
                                  String toUrl, FileSystemOptions toOptions = null, boolean overwrite) {
        return withStandardFileSystemManager({ manager ->
            try {
                FileObject from = fromOptions ? manager.resolveFile(fromUrl, fromOptions) : manager.resolveFile(fromUrl);
                FileObject to = toOptions ? manager.resolveFile(toUrl, toOptions) : manager.resolveFile(toUrl);
                if (from.exists() && from.isReadable() && (!to.exists() || overwrite)) {
                    to.copyFrom(from, Selectors.SELECT_ALL);
                }
                return to;
            } catch (FileSystemException e) {
                throw new RuntimeException(e);
            }
        } as Closure<FileObject>);
    }

    public static FileObject move(String fromUrl, FileSystemOptions fromOptions = null,
                                  String toUrl, FileSystemOptions toOptions = null, boolean overwrite) {
        return withStandardFileSystemManager({ manager ->
            try {
                FileObject from = fromOptions ? manager.resolveFile(fromUrl, fromOptions) : manager.resolveFile(fromUrl);
                FileObject to = toOptions ? manager.resolveFile(toUrl, toOptions) : manager.resolveFile(toUrl);
                if (from.exists() && from.isReadable() && from.type == FileType.FILE && to.type in [FileType.FILE, FileType.IMAGINARY] && (!to.exists() || overwrite)) {
                    from.moveTo(to);
                }
                return to;
            } catch (FileSystemException e) {
                throw new RuntimeException(e);
            }
        } as Closure<FileObject>);
    }
}
