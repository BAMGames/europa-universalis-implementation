import org.hibernate.*;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.Statistics;

import javax.naming.NamingException;
import javax.naming.Reference;
import java.io.Serializable;
import java.sql.Connection;
import java.util.Map;
import java.util.Set;

/**
 * Mock of Session factory while waiting for DBUnit.
 *
 * @author MKL.
 */
public class FalseSessionFactory implements SessionFactory {
    @Override
    public SessionFactoryOptions getSessionFactoryOptions() {
        return null;
    }

    @Override
    public SessionBuilder withOptions() {
        return null;
    }

    @Override
    public Session openSession() throws HibernateException {
        return null;
    }

    @Override
    public Session getCurrentSession() throws HibernateException {
        return null;
    }

    @Override
    public StatelessSessionBuilder withStatelessOptions() {
        return null;
    }

    @Override
    public StatelessSession openStatelessSession() {
        return null;
    }

    @Override
    public StatelessSession openStatelessSession(Connection connection) {
        return null;
    }

    @Override
    public ClassMetadata getClassMetadata(Class entityClass) {
        return null;
    }

    @Override
    public ClassMetadata getClassMetadata(String entityName) {
        return null;
    }

    @Override
    public CollectionMetadata getCollectionMetadata(String roleName) {
        return null;
    }

    @Override
    public Map<String, ClassMetadata> getAllClassMetadata() {
        return null;
    }

    @Override
    public Map getAllCollectionMetadata() {
        return null;
    }

    @Override
    public Statistics getStatistics() {
        return null;
    }

    @Override
    public void close() throws HibernateException {

    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public Cache getCache() {
        return null;
    }

    @Override
    public void evict(Class persistentClass) throws HibernateException {

    }

    @Override
    public void evict(Class persistentClass, Serializable id) throws HibernateException {

    }

    @Override
    public void evictEntity(String entityName) throws HibernateException {

    }

    @Override
    public void evictEntity(String entityName, Serializable id) throws HibernateException {

    }

    @Override
    public void evictCollection(String roleName) throws HibernateException {

    }

    @Override
    public void evictCollection(String roleName, Serializable id) throws HibernateException {

    }

    @Override
    public void evictQueries(String cacheRegion) throws HibernateException {

    }

    @Override
    public void evictQueries() throws HibernateException {

    }

    @Override
    public Set getDefinedFilterNames() {
        return null;
    }

    @Override
    public FilterDefinition getFilterDefinition(String filterName) throws HibernateException {
        return null;
    }

    @Override
    public boolean containsFetchProfileDefinition(String name) {
        return false;
    }

    @Override
    public TypeHelper getTypeHelper() {
        return null;
    }

    @Override
    public Reference getReference() throws NamingException {
        return null;
    }
}
