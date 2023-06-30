package za.ac.cput.service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import za.ac.cput.dto.UserDTO;
import za.ac.cput.model.User;

import java.util.Collection;

/**
 * @author : Thabiso Matsba
 * @Project : PharmacyApp
 * @Date :  2023/06/22
 * @Time : 17:11
 **/

public interface UserService {
    UserDTO createUser(User user);

    Collection<User> getAllUsers(String name, int page, int pageSize);

    UserDTO findById(Long id);

    boolean deleteUser(Long id);
}
