package com.example.tobacco.system;

import com.example.tobacco.model.RoleItem;
import com.example.tobacco.model.RoleRequest;
import com.example.tobacco.model.UserProfile;
import com.example.tobacco.model.UserRequest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemService {

    private final JdbcTemplate jdbcTemplate;

    public SystemService(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public List<UserProfile> listUsers() {
        return jdbcTemplate.query(
                "select u.id, u.username, u.real_name as realName, u.role_code as roleCode, r.name as roleName, u.status, DATE_FORMAT(u.created_at, '%Y-%m-%d %H:%i:%s') as createdAt from users u left join roles r on u.role_code = r.code order by u.id",
                new BeanPropertyRowMapper<UserProfile>(UserProfile.class));
    }

    public List<RoleItem> listRoles() {
        return jdbcTemplate.query(
                "select id, code, name, remark from roles order by id",
                new BeanPropertyRowMapper<RoleItem>(RoleItem.class));
    }

    public void createUser(UserRequest request) {
        jdbcTemplate.update("insert into users(username,password,real_name,role_code,status) values(?,?,?,?, 'ENABLED')", request.getUsername(), request.getPassword(), request.getRealName(), request.getRoleCode());
    }

    public void updateUserStatus(Long id, String status) {
        jdbcTemplate.update("update users set status=? where id=?", status, id);
    }

    public void createRole(RoleRequest request) {
        jdbcTemplate.update("insert into roles(code,name,remark) values(?,?,?)", request.getCode(), request.getName(), request.getRemark());
    }
}
