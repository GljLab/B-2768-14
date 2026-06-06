package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.employee.common.Result;
import com.employee.dto.BirthdayWallEmployeeVO;
import com.employee.dto.BirthdayWishRequest;
import com.employee.dto.BirthdayWishVO;
import com.employee.entity.BirthdayMessage;
import com.employee.entity.BirthdayWish;
import com.employee.entity.BirthdayWishLike;
import com.employee.entity.Employee;
import com.employee.mapper.BirthdayMessageMapper;
import com.employee.mapper.BirthdayWishLikeMapper;
import com.employee.mapper.BirthdayWishMapper;
import com.employee.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BirthdayWishService {

    private final BirthdayWishMapper wishMapper;
    private final BirthdayWishLikeMapper wishLikeMapper;
    private final EmployeeMapper employeeMapper;
    private final BirthdayMessageMapper messageMapper;

    public List<BirthdayWallEmployeeVO> getBirthdayWallEmployees(int year, int month) {
        QueryWrapper<Employee> wrapper = new QueryWrapper<>();
        wrapper.apply("MONTH(birthday_date) = {0}", month).eq("status", 1);
        List<Employee> employees = employeeMapper.selectList(wrapper);

        LocalDate today = LocalDate.now();

        return employees.stream().map(emp -> {
            BirthdayWallEmployeeVO vo = new BirthdayWallEmployeeVO();
            vo.setId(emp.getId());
            vo.setName(emp.getName());
            vo.setDepartment(emp.getDepartment());
            vo.setAvatar(emp.getAvatar());
            vo.setBirthdayDate(emp.getBirthdayDate());

            int day = emp.getBirthdayDate().getDayOfMonth();
            vo.setBirthdayDisplay(String.format("%02d-%02d", month, day));

            if (emp.getHireDate() != null) {
                vo.setHireYears(Period.between(emp.getHireDate(), LocalDate.now()).getYears());
            }

            vo.setIsToday(emp.getBirthdayDate().getMonthValue() == today.getMonthValue()
                    && emp.getBirthdayDate().getDayOfMonth() == today.getDayOfMonth());

            return vo;
        }).sorted((a, b) -> {
            int dayA = a.getBirthdayDate().getDayOfMonth();
            int dayB = b.getBirthdayDate().getDayOfMonth();
            return Integer.compare(dayA, dayB);
        }).collect(Collectors.toList());
    }

    public List<BirthdayWishVO> getWishesForEmployee(Long recipientId, Long currentEmployeeId) {
        QueryWrapper<BirthdayWish> wrapper = new QueryWrapper<>();
        wrapper.eq("recipient_id", recipientId).orderByDesc("created_at");
        List<BirthdayWish> wishes = wishMapper.selectList(wrapper);

        return wishes.stream().map(wish -> {
            BirthdayWishVO vo = new BirthdayWishVO();
            vo.setId(wish.getId());
            vo.setRecipientId(wish.getRecipientId());
            vo.setSenderId(wish.getSenderId());
            vo.setSenderName(wish.getSenderName());
            vo.setSenderAvatar(wish.getSenderAvatar());
            vo.setContent(wish.getContent());
            vo.setLikeCount(wish.getLikeCount());
            vo.setIsSystem(wish.getIsSystem());
            vo.setCreatedAt(wish.getCreatedAt());

            QueryWrapper<BirthdayWishLike> likeWrapper = new QueryWrapper<>();
            likeWrapper.eq("wish_id", wish.getId()).eq("employee_id", currentEmployeeId);
            vo.setHasLiked(wishLikeMapper.selectCount(likeWrapper) > 0);

            return vo;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Result<?> sendWish(Long senderId, BirthdayWishRequest request) {
        Employee sender = employeeMapper.selectById(senderId);

        BirthdayWish wish = new BirthdayWish();
        wish.setRecipientId(request.getRecipientId());
        wish.setSenderId(senderId);
        wish.setSenderName(sender.getName());
        wish.setSenderAvatar(sender.getAvatar());
        wish.setContent(request.getContent());
        wish.setLikeCount(0);
        wish.setIsSystem(0);
        wish.setCreatedAt(LocalDateTime.now());
        wishMapper.insert(wish);

        BirthdayMessage message = new BirthdayMessage();
        message.setRecipientId(request.getRecipientId());
        message.setRecipientType(1);
        message.setType("NEW_WISH");
        message.setTitle("收到生日祝福");
        message.setContent(sender.getName() + "给您送来了生日祝福");
        message.setRelatedId(wish.getId());
        message.setIsRead(0);
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(message);

        return Result.success(wish);
    }

    @Transactional
    public Result<?> likeWish(Long wishId, Long employeeId) {
        QueryWrapper<BirthdayWishLike> wrapper = new QueryWrapper<>();
        wrapper.eq("wish_id", wishId).eq("employee_id", employeeId);
        if (wishLikeMapper.selectCount(wrapper) > 0) {
            return Result.error("已点赞过该祝福");
        }

        BirthdayWishLike like = new BirthdayWishLike();
        like.setWishId(wishId);
        like.setEmployeeId(employeeId);
        like.setCreatedAt(LocalDateTime.now());
        wishLikeMapper.insert(like);

        BirthdayWish wish = wishMapper.selectById(wishId);
        wish.setLikeCount(wish.getLikeCount() + 1);
        wishMapper.updateById(wish);

        return Result.success();
    }

    @Transactional
    public Result<?> unlikeWish(Long wishId, Long employeeId) {
        QueryWrapper<BirthdayWishLike> wrapper = new QueryWrapper<>();
        wrapper.eq("wish_id", wishId).eq("employee_id", employeeId);
        BirthdayWishLike like = wishLikeMapper.selectOne(wrapper);
        if (like == null) {
            return Result.error("未点赞该祝福");
        }

        wishLikeMapper.deleteById(like.getId());

        BirthdayWish wish = wishMapper.selectById(wishId);
        wish.setLikeCount(Math.max(0, wish.getLikeCount() - 1));
        wishMapper.updateById(wish);

        return Result.success();
    }

    @Transactional
    public void sendSystemWish(Employee employee) {
        int hireYears = Period.between(employee.getHireDate(), LocalDate.now()).getYears();

        String content = "亲爱的" + employee.getName() + "，生日快乐！感谢您加入" + employee.getDepartment()
                + "大家庭已有" + hireYears + "年，祝您在新的一岁工作顺利、身体健康、万事如意！";

        BirthdayWish wish = new BirthdayWish();
        wish.setRecipientId(employee.getId());
        wish.setSenderId(0L);
        wish.setSenderName("系统");
        wish.setContent(content);
        wish.setLikeCount(0);
        wish.setIsSystem(1);
        wish.setCreatedAt(LocalDateTime.now());
        wishMapper.insert(wish);

        BirthdayMessage message = new BirthdayMessage();
        message.setRecipientId(employee.getId());
        message.setRecipientType(1);
        message.setType("BIRTHDAY_WISH");
        message.setTitle("生日快乐");
        message.setContent(content);
        message.setRelatedId(wish.getId());
        message.setIsRead(0);
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(message);
    }

    @Transactional
    public Result<?> sendWishFromAdmin(Long senderId, String senderName, BirthdayWishRequest request) {
        BirthdayWish wish = new BirthdayWish();
        wish.setRecipientId(request.getRecipientId());
        wish.setSenderId(senderId);
        wish.setSenderName(senderName);
        wish.setContent(request.getContent());
        wish.setLikeCount(0);
        wish.setIsSystem(0);
        wish.setCreatedAt(LocalDateTime.now());
        wishMapper.insert(wish);

        BirthdayMessage message = new BirthdayMessage();
        message.setRecipientId(request.getRecipientId());
        message.setRecipientType(2);
        message.setType("NEW_WISH");
        message.setTitle("收到生日祝福");
        message.setContent(senderName + "给您送来了生日祝福");
        message.setRelatedId(wish.getId());
        message.setIsRead(0);
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(message);

        return Result.success(wish);
    }
}
