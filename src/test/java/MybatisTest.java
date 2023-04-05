import com.chen.RedissonMain;
import com.chen.dao.ProductDao;
import com.chen.pojo.Product;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RedissonMain.class)
public class MybatisTest {

    @Autowired
    private ProductDao productDao;

    @Test
    public void testInsert(){
        Product product = new Product();
        product.setName("手机");
        product.setStock(200);

        productDao.insert(product);

        System.out.println(product.getId());
    }
}
