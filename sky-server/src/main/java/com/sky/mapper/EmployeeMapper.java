package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 新增员工
     *
     */
    @AutoFill(OperationType.INSERT)
    @Insert("insert into sky_take_out.employee (id, name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) VALUES (#{id}, #{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void add(Employee employee);

    List<Employee> select(EmployeePageQueryDTO employeePageQueryDTO);
    //禁用启用员工
    @Update("update employee set status = #{status} where id = #{id}")
    void forbiddenStatus(Integer status, Long id);
    //修改员工信息
    @AutoFill(OperationType.UPDATE)
    void updateEmployee(Employee employee);
    //查询返回
    @Select("select id,username,name,phone,sex,id_number from employee where id = #{id}")
    Employee getEmployee(Long id);

    @Update("update employee set password = #{newPassword} where id = #{empid}")
    void updatePassword(PasswordEditDTO passwordEditDTO);
}
