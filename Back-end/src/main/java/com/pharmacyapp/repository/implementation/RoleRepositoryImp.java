package com.pharmacyapp.repository.implementation;

import com.pharmacyapp.exception.ApiException;
import com.pharmacyapp.model.Role;
import com.pharmacyapp.query.RoleQuery;
import com.pharmacyapp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import com.pharmacyapp.rowmapper.RoleRowMapper;

import java.util.*;

/**
 * @author : Thabiso Matsaba
 * @Project : PharmacyApp
 * @Date : 2023/06/01
 * @Time : 15:00
 **/
@RequiredArgsConstructor
@Repository
@Slf4j
public class RoleRepositoryImp implements RoleRepository<Role> {
        private final NamedParameterJdbcTemplate jdbc;

 @Override
public Role save(Role role) {
    log.info("Saving A Role {}:", role);
    try {
        KeyHolder holder = new GeneratedKeyHolder();
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", role.getName())
                .addValue("permission", String.join(",", role.getPermissions()));
        jdbc.update(RoleQuery.INSERT_ROLE_TO_USER_QUERY, parameters, holder);
        if (holder.getKey() != null) {
            role.setId(holder.getKey().longValue());
        } else {
            throw new ApiException("No generated key found after inserting Role.");
        }
        return role;
    } catch (Exception exception) {
        log.error(exception.getMessage());
        throw new ApiException("An error occurred while saving the role. Please try again.");
    }
 }

   @Override
public Collection<Role> list(String name, int page, int pageSize) {
    log.info("Retrieving list of roles (Page: {}, PageSize: {})", page, pageSize);
    try {
        int offset = (page - 1) * pageSize;
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("offset", offset)
                .addValue("pageSize", pageSize);

        return jdbc.query(RoleQuery.SELECT_ROLES_QUERY, parameters, new RoleRowMapper());
    } catch (Exception exception) {
        log.error(exception.getMessage());
        throw new ApiException("An error occurred while retrieving the list of roles. Please try again.");
    }
}

    @Override
public Role read(Long id) {
    log.info("Retrieving role with ID: {}", id);
    try {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id);

        return jdbc.queryForObject(RoleQuery.SELECT_ROLE_BY_ID_QUERY, parameters, new RoleRowMapper());
    } catch (EmptyResultDataAccessException exception) {
        throw new ApiException("Role not found with ID: " + id);
    } catch (Exception exception) {
        log.error(exception.getMessage());
        throw new ApiException("An error occurred while retrieving the role. Please try again.");
    }
}


  @Override
public Role update(Role role) {
    log.info("Updating role: {}", role);
    try {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("roleId", role.getId())
                .addValue("name", role.getName())
                .addValue("permission", String.join(",", role.getPermissions()));

        jdbc.update(RoleQuery.UPDATE_ROLE_QUERY, parameters);
        return role;
    } catch (Exception exception) {
        log.error(exception.getMessage());
        throw new ApiException("An error occurred while updating the role. Please try again.");
    }
}

  @Override
public boolean delete(Long id) {
    log.info("Deleting role with ID: {}", id);
    try {
        SqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id);
        int rowsAffected = jdbc.update(RoleQuery.DELETE_ROLE_QUERY, parameters);
        return rowsAffected > 0;
    } catch (Exception exception) {
        log.error(exception.getMessage());
        throw new ApiException("An error occurred while deleting the role. Please try again.");
    }
}


     @Override
     public void addRoleToUser(Long userId, String roleName, Set<String> permissions) {
         log.info("Adding role {} to user id: {}", roleName, userId);
         try {
             Role role = jdbc.queryForObject(RoleQuery.SELECT_ROLE_BY_NAME_QUERY, Map.of("name", roleName), new RoleRowMapper());
             if (role != null) {
                 // Insert the role to the user
                 jdbc.update(RoleQuery.INSERT_ROLE_TO_USER_QUERY, Map.of("userId", userId, "roleId", role.getId()));
                 // Update the permissions for the user's role
                 jdbc.update(RoleQuery.UPDATE_USER_ROLE_PERMISSIONS_QUERY, Map.of("roleId", role.getId(), "permission", String.join(",", permissions)));
             } else {
                 throw new ApiException("No role found by name: " + roleName);
             }
         } catch (EmptyResultDataAccessException exception) {
             throw new ApiException("No role found by name: " + roleName);
         } catch (Exception exception) {
             log.error(exception.getMessage());
             throw new ApiException("An error occurred. Please try again.");
         }
     }



     @Override
    public List<Role> getRolesByUserId(Long userId) {
        log.info("Retrieving roles by user ID: {}", userId);
        try {
            SqlParameterSource parameters = new MapSqlParameterSource().addValue("userId", userId);
            return jdbc.query(RoleQuery.SELECT_ROLES_BY_USER_ID_QUERY, parameters, new RoleRowMapper());
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            return Collections.emptyList();
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred while retrieving the roles. Please try again.");
        }
    }


   @Override
public Role getRoleByUserEmail(String email) {
    log.info("Retrieving role by user email: {}", email);
    try {
        SqlParameterSource parameters = new MapSqlParameterSource().addValue("email", email);
        return jdbc.queryForObject(RoleQuery.SELECT_ROLE_BY_USER_EMAIL_QUERY, parameters, new RoleRowMapper());
    } catch (EmptyResultDataAccessException exception) {
        return null; // Handle the case where no role is found for the given user email
    } catch (Exception exception) {
        log.error(exception.getMessage());
        throw new ApiException("An error occurred while retrieving the role. Please try again.");
    }
}


 @Override
 public Role updateUserRole(Long userId, String roleName) {
     log.info("Updating role of user with ID: {} to role: {}", userId, roleName);
     try {
         Role role = jdbc.queryForObject(RoleQuery.SELECT_ROLE_BY_NAME_QUERY, Map.of("name", roleName), new RoleRowMapper());
         if (role == null) {
             throw new ApiException("Role not found. Please provide a valid role name.");
         }
         // Fetch permissions associated with the role
         Set<String> permissions = role.getPermissions(); // Assuming your Role class has a 'permissions' field
         // Update user's permissions in the database
         SqlParameterSource parameters = new MapSqlParameterSource()
                 .addValue("userId", userId)
                 .addValue("roleId", role.getId())
                 .addValue("permissions", String.join(",", permissions)); // Convert permissions set to comma-separated string
         jdbc.update(RoleQuery.UPDATE_USER_ROLE_QUERY, parameters);
         return role;
     } catch (EmptyResultDataAccessException exception) {
         throw new ApiException("No role found by name: " + roleName);
     } catch (Exception exception) {
         log.error(exception.getMessage());
         throw new ApiException("An error occurred while updating the user role. Please try again.");
     }
 }


     @Override
     public Role findRoleByName(String roleName) {
         String query = "SELECT id FROM Roles WHERE name = :roleName";
         Map<String, Object> params = new HashMap<>();
         params.put("roleName", roleName);
         return jdbc.queryForObject(query, params, Role.class);
     }

     @Override
     public Long findUserIdByEmail(String email) {
         String query = "SELECT id FROM Users WHERE email = :email";
         Map<String, Object> params = new HashMap<>();
         params.put("email", email);
         return jdbc.queryForObject(query, params, Long.class);
     }

     @Override
     public Long findRoleIdByName(String roleName) {
         String query = "SELECT id FROM Roles WHERE name = :roleName";
         Map<String, Object> params = new HashMap<>();
         params.put("roleName", roleName);
         return jdbc.queryForObject(query, params, Long.class);
     }

     public void assignRolesToUser(String email) {
         // Fetch user ID based on email
         Long userId = findUserIdByEmail(email);
         if (userId == null) {
             // Handle user not found
             return;
         }
         // Fetch role IDs for ROLE_ADMIN, ROLE_MANAGER, and ROLE_SYSADMIN
         Long roleIdAdmin = findRoleIdByName("ROLE_ADMIN");
         Long roleIdManager = findRoleIdByName("ROLE_MANAGER");
         Long roleIdSysAdmin = findRoleIdByName("ROLE_SYSADMIN");
         // Insert records into UserRoles table to assign roles to the user
         String assignRolesQuery = "INSERT INTO UserRoles (user_id, role_id) VALUES (:userId, :roleId)";
         Map<String, Object> params = new HashMap<>();
         params.put("userId", userId);
         // Assign ROLE_ADMIN
         params.put("roleId", roleIdAdmin);
         jdbc.update(assignRolesQuery, params);
         // Assign ROLE_MANAGER
         params.put("roleId", roleIdManager);
         jdbc.update(assignRolesQuery, params);
         // Assign ROLE_SYSADMIN
         params.put("roleId", roleIdSysAdmin);
         jdbc.update(assignRolesQuery, params);
     }

 }
