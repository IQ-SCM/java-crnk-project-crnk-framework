package io.crnk.data.jpa.repository;

import io.crnk.client.CrnkClient;
import io.crnk.client.http.inmemory.InMemoryHttpAdapter;
import io.crnk.client.internal.proxy.ObjectProxy;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.data.jpa.model.TestEmbeddedIdEntity;
import io.crnk.data.jpa.model.TestEntity;
import io.crnk.data.jpa.model.TestIdEmbeddable;
import io.crnk.data.jpa.query.AbstractJpaTest;
import io.crnk.data.jpa.query.JpaQueryFactory;
import io.crnk.data.jpa.query.criteria.JpaCriteriaQueryFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@Transactional
public class EmbeddedIdJpaTest extends AbstractJpaTest {

    private ResourceRepository<TestEntity, Long> testRepository;

    private ResourceRepository<TestEmbeddedIdEntity, TestIdEmbeddable> embeddedRepository;

    @Override
    @Before
    public void setup() {
        super.setup();

        InMemoryHttpAdapter adapter = new InMemoryHttpAdapter(boot, "http://localhost:1234");
        CrnkClient client = new CrnkClient("http://localhost:1234");
        client.findModules();
        client.setHttpAdapter(adapter);

        testRepository = client.getRepositoryForType(TestEntity.class);
        embeddedRepository = client.getRepositoryForType(TestEmbeddedIdEntity.class);
    }

    @Test
    public void checkFindAll() {
        ResourceList<TestEmbeddedIdEntity> list = embeddedRepository.findAll(new QuerySpec(TestEmbeddedIdEntity.class));
        Assert.assertEquals(numTestEntities, list.size());
        for (TestEmbeddedIdEntity entity : list) {
            TestIdEmbeddable id = entity.getId();
            Assert.assertNotNull(id);
            Assert.assertNotNull(id.getEmbIntValue());
            Assert.assertNotNull(id.getEmbStringValue());
            Assert.assertNull(entity.getTestEntity());
        }
    }

    @Test
    public void checkIncludeRelated() {
        QuerySpec querySpec = new QuerySpec(TestEmbeddedIdEntity.class);
        querySpec.includeRelation(PathSpec.of("testEntity"));
        ResourceList<TestEmbeddedIdEntity> list = embeddedRepository.findAll(querySpec);
        Assert.assertEquals(numTestEntities, list.size());
        for (TestEmbeddedIdEntity entity : list) {
            TestIdEmbeddable id = entity.getId();
            Assert.assertNotNull(id);
            Assert.assertNotNull(entity.getTestEntity());
        }
    }

    @Test
    public void checkIncludeAsRelated() {
        QuerySpec querySpec = new QuerySpec(TestEntity.class);
        querySpec.includeRelation(PathSpec.of("embeddedIdEntities"));
        ResourceList<TestEntity> list = testRepository.findAll(querySpec);
        Assert.assertEquals(numTestEntities, list.size());
        for (TestEntity entity : list) {
            List<TestEmbeddedIdEntity> embeddedIdEntities = entity.getEmbeddedIdEntities();
            Assert.assertFalse(embeddedIdEntities instanceof ObjectProxy);
            Assert.assertEquals(1, embeddedIdEntities.size());
            TestEmbeddedIdEntity embeddedIdEntity = embeddedIdEntities.get(0);
            Assert.assertNotNull(embeddedIdEntity.getId());
            Assert.assertNotNull(embeddedIdEntity.getId().getEmbStringValue());
            Assert.assertNotNull(embeddedIdEntity.getId().getEmbIntValue());
        }
    }

    @Test
    public void checkLazyLoadAsRelated() {
        QuerySpec querySpec = new QuerySpec(TestEntity.class);
        ResourceList<TestEntity> list = testRepository.findAll(querySpec);
        Assert.assertEquals(numTestEntities, list.size());
        for (TestEntity entity : list) {
            List<TestEmbeddedIdEntity> embeddedIdEntities = entity.getEmbeddedIdEntities();
            Assert.assertTrue(embeddedIdEntities instanceof ObjectProxy);
            Assert.assertEquals(1, embeddedIdEntities.size());
            TestEmbeddedIdEntity embeddedIdEntity = embeddedIdEntities.get(0);
            Assert.assertNotNull(embeddedIdEntity.getId());
            Assert.assertNotNull(embeddedIdEntity.getId().getEmbStringValue());
            Assert.assertNotNull(embeddedIdEntity.getId().getEmbIntValue());
        }
    }

    @Override
    protected JpaQueryFactory createQueryFactory(EntityManager em) {
        return JpaCriteriaQueryFactory.newInstance();
    }

}
