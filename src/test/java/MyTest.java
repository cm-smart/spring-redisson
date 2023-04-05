import com.chen.RedissonMain;
import com.chen.pojo.Product;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RedissonMain.class)
public class MyTest {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void test01(){
        String[] names = applicationContext.getBeanDefinitionNames();
        for(String name:names){
            System.out.println(name);
        }
    }

    @Test
    public void test02(){
        RBucket<String> bucket = redissonClient.getBucket("name");
        bucket.set("chenmin");

        String product = bucket.get();
        System.out.println(product);
    }

    @Test
    public void test03(){
        Product product = new Product();
        product.setId(1L);
        product.setName("手机");
        product.setStock(100);
        RBucket<Product> bucket = redissonClient.getBucket("product");
        bucket.set(product);
        Product temp = bucket.get();
        System.out.println(temp.getName());
    }

    @Test
    public void testMap(){
        Product product = new Product();
        product.setId(1L);
        product.setName("手机");
        product.setStock(100);

        RMap<String,Product> map = redissonClient.getMap("product");
        map.put("001",product);

    }

    @Test
    public void test04(){
        redisTemplate.opsForValue().set("name:02","陈敏");
        Object name = redisTemplate.opsForValue().get("name:02");
        System.out.println(name);
    }
}
