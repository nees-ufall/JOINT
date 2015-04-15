package wwwc.nees.joint.module.kao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Abstract class KAO, for operations in the persistence with SESAME and Alibaba
 *
 * @author Olavo Holanda
 * @version 1.0 - 15/01/2012
 */
public abstract class AbstractKAO {

    // VARIABLES
    // -------------------------------------------------------------------------
    // Variable to connect with the repository
    private Repository repository;
    // Variable to do operations in the repository
    private RepositoryConnection con;
    // Variable with the desired class to be implemented
    private Class<?> classe;
    // Interface to perform queries in the repository
    private QueryRunner queryRunner;
    // URI[] of graph to save triples
    private URI[] contexts;

    // CONSTRUCTOR
    // -------------------------------------------------------------------------
    /**
     * Class Constructor, starts the <code>Repository</code> and creates a
     * <code>ObjectConnection</code>, to do persistence operations.
     *
     * @param classe the class to be implemented.
     * @param ontologyURI the ontology URI (not the namespace), where
     * persistence operations will be done.
     *
     */
    public <T> AbstractKAO(Class<T> classe) {

        this.classe = classe;

        // Retrieves the repository in the server
        this.repository = RepositoryFactory.getRepository();
//        try {
//            this.con = this.repository.getConnection();
//        } catch (RepositoryException ex) {
//            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, ex);
//        }
        // Creates a QueryRunner with SPARQL implementation
        this.queryRunner = new SPARQLQueryRunnerImpl(this.repository);
        this.contexts = new URI[]{};
    }

    // METHODS
    // -------------------------------------------------------------------------
    /**
     * Creates a new instance in the repository with the specified name.
     *
     * @param instanceName a <code>String</code> with the instance name.
     * @param ontologyURI a <code>String</code> with the instance name.
     * @return T the new instance.
     */
    public <T> T create(String ontologyURI, String instanceName, URI... contexts) {
        setContexts(contexts);
        CreateOperations createOpe = new CreateOperations();

        Object ob = null;
        try {
            con = this.repository.getConnection();
            try {
                con.setAutoCommit(false);

                ob = createOpe.create(ontologyURI, instanceName, this.classe, con, this.getContexts());
                con.commit();

            } catch (Exception e) {
                // If throws any exception rollback
                con.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                con.close();
            }
        } catch (RepositoryException eR) {
            // If throws repository Exception the the connection is not inialized
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, eR);
        }
        return (T) ob;
    }

    /**
     * Retrieve contexts.
     *
     * @return a <code>URI[]</code> with contexts.
     */
    public URI[] getContexts() {
        return contexts;
    }

    /**
     * Set contexts.
     *
     * @param contexts a <code>URI[]</code> with contexts.
     */
    public void setContexts(URI[] contexts) {
        this.contexts = (contexts == null) ? new URI[]{} : contexts;
    }

    /**
     * Creates a new instance with a unique ID in the repository with the
     * specified prefix.
     *
     * @param instancePrefix a <code>String</code> with the prefix name.
     * @param ontologyURI a <code>String</code> with the instance name.
     * @return T the new instance.
     */
    public <T> T createWithUniqueID(String ontologyURI, String instancePrefix, URI... contexts) {
        setContexts(contexts);
        CreateOperations createOpe = new CreateOperations();

        Object ob = null;
        try {
            con = this.repository.getConnection();
            try {
                con.setAutoCommit(false);
                ob = createOpe.createWithUniqueID(ontologyURI, instancePrefix, this.classe, con, this.getContexts());
                con.commit();

            } catch (Exception e) {
                // If throws any exception rollback
                con.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                con.close();
            }
        } catch (RepositoryException eR) {
            // If throws repository Exception the the connection is not inialized
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, eR);
        }
        return (T) ob;
    }

    /**
     * Removes the desired instance in the repository, must be saved after.
     *
     * @param instanceName a <code>String</code> with the instance name.
     */
    public void delete(String ontologyURI, String instanceName, URI... contexts) {
        setContexts(contexts);

        try {
            con = this.repository.getConnection();

            try {
                //gets connection
                con.setAutoCommit(false);
                RemoveOperations removeOpe = new RemoveOperations();
                //removes the quads that have the corresponding subject 
//                removeOpe.remove(ontologyURI, instanceName, con, this.getContexts());
                String subj = ontologyURI + instanceName;
                removeOpe.remove_SPARQLUpdate(con, subj, this.getContexts());

                // Saves the object in the repository
                con.commit();
            } catch (Exception e) {
                // If throws any exception rollback
                con.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                con.close();
            }
        } catch (RepositoryException eR) {
            // If throws repository Exception the the connection is not inialized
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, eR);
        }
    }

    /**
     * Removes the desired instance in the repository, must be saved after.
     *
     * @param instance an object T with the instance
     * @param contexts the graphs in which the instance is removed.
     */
    public <T> void delete(T instance, URI... contexts) {
        try {
            setContexts(contexts);

            con = this.repository.getConnection();

            RemoveOperations removeOpe = new RemoveOperations();

            try {
                //gets connection
                con.setAutoCommit(false);

                removeOpe.remove_SPARQLUpdate(con, instance.toString(), this.getContexts());
//                removeOpe.remove(instance, con, this.getContexts());

                // Saves the object in the repository
                con.commit();

            } catch (Exception e) {
                // If throws any exception rollback
                con.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                con.close();
            }
        } catch (RepositoryException eR) {
            // If throws repository Exception the the connection is not inialized
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, eR);
        }
    }

    /**
     * Retrieves the desired instance in the repository.
     *
     * @param ontologyURI : a <code>String</code> with the ontology base URI
     * @param instanceName a <code>String</code> with the instance name.
     * @param contexts the graphs in which the instance is removed.
     * @return T the desired instance.
     */
    public <T> T retrieveInstance(String ontologyURI, String instanceName, URI... contexts) {
        Object ob = null;
        try {
            setContexts(contexts);
            con = this.repository.getConnection();
            RetrieveOperations retrieveOpe = new RetrieveOperations(con);
            try {
                //gets connection
                con.setAutoCommit(false);

                ob = retrieveOpe.retrieveInstance(ontologyURI, instanceName, classe, con, this.getContexts());

                // Saves the object in the repository
                con.commit();
            } catch (Exception e) {
                // If throws any exception rollback
                con.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                con.close();
            }
        } catch (RepositoryException eR) {
            // If throws repository Exception the the connection is not inialized
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, eR);
        }
        return (T) ob;
    }

    /**
     * Retrieves all the instances of the class, passed in the constructor.
     *
     * @return <code>List<T></code> a List with the instances.
     */
    public <T> List<T> retrieveAllInstances(URI... contexts) {
        setContexts(contexts);
        // Creates a new java.util.List
        List<T> listInstances = new ArrayList<>();

        try {
            con = this.repository.getConnection();
            RetrieveOperations retrieveOpe = new RetrieveOperations(con);
            try {
                //gets connection
                con.setAutoCommit(false);

                listInstances = (List<T>) retrieveOpe.retrieveAllInstances(classe, con, this.getContexts());

                // Saves the object in the repository
                con.commit();
            } catch (Exception e) {
                // If throws any exception rollback
                con.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                con.close();
            }
        } catch (RepositoryException eR) {
            // If throws repository Exception the the connection is not inialized
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, eR);
        }

        return listInstances;
    }

    /**
     * Saves the uncommitted changes in the repository and close the connection
     * with it.
     *
     */
    public <T> T update(T instance, URI... contexts) {
        setContexts(contexts);
        Object ob = null;
        UpdateOperations updateOpe = new UpdateOperations();

        try {
            //gets connection
            con = this.repository.getConnection();
            try {
                con.setAutoCommit(false);
                ob = updateOpe.updateDettachedInstance(instance, classe, con, this.getContexts());

                // Saves the object in the repository
                con.commit();
            } catch (Exception e) {
                // If throws any exception rollback
                con.rollback();
                Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                con.close();
            }
        } catch (RepositoryException eR) {
            // If throws repository Exception the the connection is not inialized
            Logger.getLogger(AbstractKAO.class.getName()).log(Level.SEVERE, null, eR);
        }
        return (T) ob;
    }

    /**
     * Performs queries in the repository, returning a single result.
     *
     * @param query the <code>String</code> with the query to be performed.
     *
     * @return object <code>Object</code> result.
     */
    public Object executeSPARQLquerySingleResult(String query) {
        setContexts(this.retrieveContexts(query));

        return this.queryRunner.executeQueryAsSingleResult(query, contexts);
    }

    /**
     * Performs queries in the repository, returning a java.util.List of
     * results.
     *
     * @param query the <code>String</code> with the query to be performed.
     *
     * @return <code>List<Object></code> a java.util.List with the results.
     */
    public List executeSPARQLqueryResultList(String query) {
        setContexts(this.retrieveContexts(query));
        return this.queryRunner.executeQueryAsList(query, this.getContexts());
    }

    /**
     * Performs queries in the repository, returning a java.util.Iterator with
     * the results.
     *
     * @param query the <code>String</code> with the query to be performed.
     * @param contexts <code>URI</code> represents the graphs that will be
     * queried
     *
     * @return <code>Iterator<Object></code> a java.util.List with the results.
     */
    public Iterator executeQueryAsIterator(String query, URI... contexts) {
        Set<URI> ctx = new HashSet<>();
        
        for (URI ctx1 : contexts) {
            ctx.add(ctx1);
        }
        
        for (URI ctx1 : this.retrieveContexts(query)) {
            ctx.add(ctx1);
        }
        
        setContexts((URI[]) ctx.toArray());
        return this.queryRunner.executeQueryAsIterator(query, this.getContexts());
    }

    /**
     * Performs SPARQL queries in the repository, returning a boolean with the
     * result.
     *
     * @param query the <code>String</code> with the query to be performed.
     *
     * @return <code>boolean<Object></code> true or false.
     */
    public boolean executeBooleanQuery(String query) {
        return this.queryRunner.executeBooleanQuery(query);
    }

    /**
     * Performs SPARQL update queries in the repository, returning a boolean
     * true if the query was performed with successful or false otherwise.
     *
     * @param query the <code>String</code> with the query to be performed.
     * @return <code>boolean</code> true or false.
     */
    public boolean executeSPARQLUpdateQuery(String query) {
        return this.queryRunner.executeUpdateQuery(query);
    }

    /**
     * Changes the class that will be used for CRUD operations.
     *
     * @param classe the class to be implemented.
     */
    public <T> void setClasse(Class<T> classe) {
        this.classe = classe;
    }

    /**
     * Retrieves the current class that will be used for CRUD operations.
     *
     * @return classe the class to be implemented.
     */
    public Class<?> retrieveClass() {
        return this.classe;
    }

    /**
     * Retrieve contexts from a sparql query.
     * @param sparqlQuery a <code>String</code> with a sparql query.
     * @return a <code>URI[]</code> with contexts.
     */
    public URI[] retrieveContexts(String sparqlQuery) {
        Pattern p = Pattern.compile("([\\s]+from[\\s]+[\\<](.+)[\\>])");
        ArrayList<URI> contexts = new ArrayList<>();
        Matcher m = p.matcher(sparqlQuery);
        
        while (m.find()) {
            contexts.add(new URIImpl(m.group(2)));
        }
        return (URI[]) contexts.toArray();
    }

    private void ArrayList(URI[] contexts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}