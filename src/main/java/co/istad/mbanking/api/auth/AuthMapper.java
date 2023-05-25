package co.istad.mbanking.api.auth;

import co.istad.mbanking.api.user.Role;
import co.istad.mbanking.api.user.User;
import co.istad.mbanking.api.user.web.Authority;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Mapper
@Repository
public interface AuthMapper {

    @InsertProvider(type = AuthProvider.class, method = "buildRegisterSql")
    @Options(useGeneratedKeys = true,keyColumn = "id",keyProperty = "id")
    boolean register(@Param("u") User user);

    @InsertProvider(type = AuthProvider.class, method = "buildRegisterCreateUserRoleSql")
    void createUserRole(@Param("userId") Integer userId, @Param("roleId") Integer roleId);

    @Select("SELECT * FROM users WHERE email=#{e} AND is_deleted=FALSE")
    @Results(id = "authResult", value = {
            @Result(column = "id",property = "id"),
            @Result(column = "student_card_id",property = "studentCardId"),
            @Result(column = "is_student",property = "isStudent"),
            @Result(column = "is_verified",property = "isVerified"),
            @Result(column = "verified_code",property = "verifiedCode"),
            @Result(column = "id",property = "roles",many = @Many(select = "loadUserRoles"))
    })
    Optional<User> selectByEmail(@Param("e") String email);

    @Select("SELECT * FROM users WHERE email=#{e} AND is_deleted=FALSE AND is_verified=TRUE")
    @ResultMap("authResult")
    Optional<User> loadUserByUsername(@Param("e") String email);

    @SelectProvider(type = AuthProvider.class, method = "buildSelectByEmailAndVerifiedCodeSql")
    @ResultMap("authResult")
    Optional<User> selectByEmailAndVerifiedCode(@Param("email") String email,@Param("verifiedCode") String verifiedCode);

    @UpdateProvider(type = AuthProvider.class, method = "buildUpdateIsVerifyStatusSql")
    void updateIsVerifyStatus(@Param("email") String email,@Param("verifiedCode") String verifiedCode);

    @UpdateProvider(type = AuthProvider.class, method = "buildUpdateVerifiedCodeSql")
    Boolean updateVerifiedCode(@Param("email") String email,@Param("verifiedCode") String verifiedCode);


    @SelectProvider(type = AuthProvider.class, method = "buildLoadUserRolesSql")
    @Result(column = "id", property = "authorities",
        many = @Many(select = "loadUserAuthorities"))
    List<Role> loadUserRoles(@Param("id") Integer id);

    @SelectProvider(type = AuthProvider.class, method = "buildLoadUserAuthoritiesSql")
    List<Authority> loadUserAuthorities(@Param("id") Integer roleId);




}
