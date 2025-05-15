package gr.aueb.cf.schoolapp.dao;

import gr.aueb.cf.schoolapp.model.IdentifiableEntity;
import gr.aueb.cf.schoolapp.service.util.JPAHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.Setter;

import java.util.*;

/**
 *
 * @param <T>
 */
public abstract class AbstractDAO<T extends IdentifiableEntity> implements IGenericDAO<T> {

    //    private final Class<T> persistenceClass;
    private Class<T> persistenceClass;

    public AbstractDAO() {

    }

//    public AbstractDAO(Class<T> persistenceClass) {
//        this.persistenceClass = persistenceClass;
//    }

    public Class<T> getPersistenceClass() {
        return persistenceClass;
    }

    public void setPersistenceClass(Class<T> persistenceClass) {
        this.persistenceClass = persistenceClass;
    }

    @Override
    public Optional<T> insert(T t) {
        EntityManager em = getEntityManager();
        em.persist(t);
        return Optional.of(t);
    }

    @Override
    public Optional<T> update(T t) {
        EntityManager em = getEntityManager();
        em.merge(t);
        return Optional.of(t);
    }

//        Optional<T> toUpdate = getById(t.getId());
//        if (toUpdate.isPresent()) {
//            em.merge(t);
//            return Optional.of(t);
//        }
//
//        return Optional.empty();
//    }

    @Override
    public void delete(Object id) {
        EntityManager em = getEntityManager();
        Optional<T> toDelete = getById(id);
        toDelete.ifPresent(em::remove);
    }

    @Override
    public Optional<T> getById(Object id) {
        EntityManager em = getEntityManager();
        return Optional.ofNullable(em.find(persistenceClass, id));
    }

    // Method to count a table.
    @Override
    public long count() {
        return getEntityManager()
                .createQuery("SELECT COUNT(e) FROM " + persistenceClass.getSimpleName() + " e", Long.class)
                .getSingleResult();
    }

    @Override
    public long getCountByCriteria(Map<String, Object> criteria) {
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<T> entityRoot = countQuery.from(persistenceClass);

        List<Predicate> predicates = getPredicatesList(builder, entityRoot, criteria);
        countQuery.select(builder.count(entityRoot))
                .where(predicates.toArray(new Predicate[0]));

        return getEntityManager()
                .createQuery(countQuery)
                .getSingleResult();
    }


    @Override
    public List<T> getAll() {
        return getByCriteria(getPersistenceClass(), Collections.emptyMap());
    }

    @Override
    public List<T> getByCriteria(Map<String, Object> criteria) {
        return getByCriteria(getPersistenceClass(), criteria);
    }

    @Override
    public List<T> getByCriteria(Class<T> clazz, Map<String, Object> criteria) {
        EntityManager em = getEntityManager();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> selectQuery = builder.createQuery(clazz);
        Root<T> entityRoot = selectQuery.from(clazz);

        List<Predicate> predicates = getPredicatesList(builder, entityRoot, criteria);
        selectQuery.select(entityRoot).where(predicates.toArray(new Predicate[0]));
        TypedQuery<T> query = em.createQuery(selectQuery);
        addParametersToQuery(query, criteria);
//        List<T> entitiesToReturn = query.getResultList();
//        if (entitiesToReturn != null) System.out.println("IN getByCriteriaDAO" + Arrays.toString(entitiesToReturn.toArray()));
//        else System.out.println("IS NULL");
//        return  entitiesToReturn;
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    protected List<Predicate> getPredicatesList(CriteriaBuilder builder, Root<T> entityRoot, Map<String , Object> criteria) {
        List<Predicate> predicates = new ArrayList<>();

        for (Map.Entry<String, Object> entry : criteria.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            ParameterExpression<?> val = builder.parameter(value.getClass(), buildParameterAlias(key));
//            Predicate equal = builder.equal(resolvePath(entityRoot, key), val);
            Predicate predicateLike = builder.like((Expression<String>) resolvePath(entityRoot, key), (Expression<String>) val);
//            predicates.add(equal);
            predicates.add(predicateLike);
        }
        return predicates;
    }

    protected Path<?> resolvePath(Root<T> root, String expression) {
        String[] fields = expression.split("\\.");
        Path<?> path = root.get(fields[0]);
        for (int i = 1; i < fields.length; i++) {
            path = path.get(fields[i]);
        }
        return path;
    }

    protected void addParametersToQuery(TypedQuery<?> query, Map<String , Object> criteria) {
        for (Map.Entry<String , Object> entry : criteria.entrySet()) {
            Object value = entry.getValue();
            query.setParameter(buildParameterAlias(entry.getKey()), value + "%");
        }
    }

    protected String buildParameterAlias(String alias) {
        return alias.replaceAll("\\.", "");
    }

//    @Override
//    public List<T> getByCriteria(Class<T> clazz, Map<String, Object> criteria) {
//        EntityManager em = getEntityManager();
//        CriteriaBuilder builder = em.getCriteriaBuilder();
//        CriteriaQuery<T> selectQuery = builder.createQuery(clazz);
//        Root<T> entityRoot = selectQuery.from(clazz);
//
//        // Generate predicates based on the criteria map
//        List<Predicate> predicates = getPredicatesList(builder, entityRoot, criteria);
//        selectQuery.select(entityRoot).where(predicates.toArray(new Predicate[0]));
//
//        TypedQuery<T> query = em.createQuery(selectQuery);
//        addParametersToQuery(query, criteria);
//
//        return query.getResultList();
//    }
//
//    @SuppressWarnings("unchecked")
//    protected List<Predicate> getPredicatesList(CriteriaBuilder builder, Root<T> entityRoot, Map<String, Object> criteria) {
//        List<Predicate> predicates = new ArrayList<>();
//
//        for (Map.Entry<String, Object> entry : criteria.entrySet()) {
//            String key = entry.getKey();
//            Object value = entry.getValue();
//
//            // Handling the cases where the value is a List, Map or a "isNull" condition
//            if (value instanceof List) {
//                Path<?> path = resolvePath(entityRoot, key);
//                CriteriaBuilder.In<Object> inClause = builder.in(path);
//                for (Object v : (List<?>) value) {
//                    inClause.value(v);
//                }
//                predicates.add(inClause);
//            } else if (value instanceof Map) {
//                // For 'BETWEEN' condition
//                Map<String, Object> mapValue = (Map<String, Object>) value;
//                if (mapValue.containsKey("from") && mapValue.containsKey("to")) {
//                    Object from = mapValue.get("from");
//                    Object to = mapValue.get("to");
//
//                    if (from instanceof Comparable && to instanceof Comparable) {
//                        Expression<? extends Comparable<Object>> path =
//                                (Expression<? extends Comparable<Object>>) resolvePath(entityRoot, key);
//
//                        predicates.add(builder.between(path, (Comparable<Object>) from, (Comparable<Object>) to));
//                    }
//                }
//            } else if ("isNull".equals(value)) {
//                // For 'IS NULL' condition
//                predicates.add(builder.isNull(resolvePath(entityRoot, key)));
//            } else if ("isNotNull".equals(value)) {
//                // For 'IS NOT NULL' condition
//                predicates.add(builder.isNotNull(resolvePath(entityRoot, key)));
//            } else if (value instanceof String && ((String) value).contains("%")) {
//                // Treat as LIKE pattern (e.g., "Jo%")
//                predicates.add(
//                        builder.like(
//                                builder.lower((Expression<String>) resolvePath(entityRoot, key)),
//                                ((String) value).toLowerCase()
//                        ));
//            } else {
//                // For '=' condition (default case)
//                predicates.add(builder.equal(resolvePath(entityRoot, key), value));
//            }
//        }
//        return predicates;
//    }
//
//    protected Path<?> resolvePath(Root<T> root, String expression) {
//        String[] fields = expression.split("\\.");
//        Path<?> path = root.get(fields[0]);
//        for (int i = 1; i < fields.length; i++) {
//            path = path.get(fields[i]);
//        }
//        return path;
//    }
//
//    protected void addParametersToQuery(TypedQuery<?> query, Map<String, Object> criteria) {
//        for (Map.Entry<String, Object> entry : criteria.entrySet()) {
//            Object value = entry.getValue();
//            if (value instanceof List || value instanceof Map) {
//                // Handle complex cases like IN or BETWEEN that need special parameter setting
//                // (    Do not add % for LIKE here)
//                query.setParameter(buildParameterAlias(entry.getKey()), value);
//            } else {
//                // Adding '%' for LIKE operations if needed
//                query.setParameter(buildParameterAlias(entry.getKey()) , value + "%");
//            }
//        }
//    }

    @Override
    public List<T> getByCriteriaPaginated(Class<T> clazz, Map<String, Object> criteria, Integer page, Integer size) {
        EntityManager em = getEntityManager();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> selectQuery = builder.createQuery(clazz);
        Root<T> entityRoot = selectQuery.from(clazz);

        // Build predicates
        List<Predicate> predicates = getPredicatesList(builder, entityRoot, criteria);
        selectQuery.select(entityRoot).where(predicates.toArray(new Predicate[0]));

        // Create query and apply pagination
        TypedQuery<T> query = em.createQuery(selectQuery);

        if (page != null && size != null) {
            query.setFirstResult(page * size);      // skip
            query.setMaxResults(size);
        }
        return query.getResultList();
    }


    public EntityManager getEntityManager() {
        return JPAHelper.getEntityManager();
    }

    @Override
    public Optional<T> findByField(String fieldName, Object value) {

        // Build the dynamic JPQL query to find by the given field and value
        String queryString = "SELECT e FROM " + persistenceClass.getSimpleName() + " e WHERE e." + fieldName + " = :value";

        // Create the TypedQuery with the dynamically built query string
        TypedQuery<T> query = getEntityManager().createQuery(queryString, persistenceClass);

        // Set the parameter value
        query.setParameter("value", value);

        // Execute the query and return the first result wrapped in an Optional
        return query.getResultList().stream().findFirst();
    }
}