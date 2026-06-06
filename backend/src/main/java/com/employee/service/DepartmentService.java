package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.employee.dto.DepartmentRequest;
import com.employee.entity.Department;
import com.employee.entity.Employee;
import com.employee.mapper.DepartmentMapper;
import com.employee.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentMapper departmentMapper;
    private final EmployeeMapper employeeMapper;
    
    public List<Department> getDepartmentTree() {
        List<Department> allDepartments = departmentMapper.selectList(
            new LambdaQueryWrapper<Department>().orderByAsc(Department::getSortOrder).orderByAsc(Department::getId)
        );
        
        Map<Long, Employee> employeeMap = new HashMap<>();
        List<Long> managerIds = allDepartments.stream()
            .filter(d -> d.getManagerId() != null)
            .map(Department::getManagerId)
            .distinct()
            .collect(Collectors.toList());
        if (!managerIds.isEmpty()) {
            List<Employee> managers = employeeMapper.selectBatchIds(managerIds);
            employeeMap = managers.stream().collect(Collectors.toMap(Employee::getId, e -> e));
        }
        
        Map<Long, Department> departmentMap = new HashMap<>();
        for (Department dept : allDepartments) {
            dept.setChildren(new ArrayList<>());
            if (dept.getManagerId() != null && employeeMap.containsKey(dept.getManagerId())) {
                dept.setManagerName(employeeMap.get(dept.getManagerId()).getName());
            }
            int directCount = departmentMapper.countDirectMembers(dept.getId());
            dept.setDirectMemberCount(directCount);
            dept.setTotalMemberCount(0);
            departmentMap.put(dept.getId(), dept);
        }
        
        List<Department> rootDepartments = new ArrayList<>();
        for (Department dept : allDepartments) {
            if (dept.getParentId() == null) {
                rootDepartments.add(dept);
            } else {
                Department parent = departmentMap.get(dept.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dept);
                }
            }
        }
        
        calculateTotalMemberCount(rootDepartments);
        
        return rootDepartments;
    }
    
    private int calculateTotalMemberCount(List<Department> departments) {
        int total = 0;
        for (Department dept : departments) {
            int deptTotal = dept.getDirectMemberCount();
            if (dept.getChildren() != null && !dept.getChildren().isEmpty()) {
                deptTotal += calculateTotalMemberCount(dept.getChildren());
            }
            dept.setTotalMemberCount(deptTotal);
            total += deptTotal;
        }
        return total;
    }
    
    public Department getDepartmentById(Long id) {
        Department dept = departmentMapper.selectById(id);
        if (dept != null && dept.getManagerId() != null) {
            Employee manager = employeeMapper.selectById(dept.getManagerId());
            if (manager != null) {
                dept.setManagerName(manager.getName());
            }
        }
        return dept;
    }
    
    @Transactional
    public Department createDepartment(DepartmentRequest request) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getCode, request.getCode());
        if (departmentMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("部门编码已存在");
        }
        
        if (request.getParentId() != null) {
            Department parent = departmentMapper.selectById(request.getParentId());
            if (parent == null) {
                throw new RuntimeException("上级部门不存在");
            }
        }
        
        if (request.getManagerId() != null) {
            Employee manager = employeeMapper.selectById(request.getManagerId());
            if (manager == null || manager.getStatus() != 1) {
                throw new RuntimeException("责任人必须是在职员工");
            }
        }
        
        Department department = new Department();
        department.setName(request.getName());
        department.setCode(request.getCode());
        department.setParentId(request.getParentId());
        department.setManagerId(request.getManagerId());
        department.setRemark(request.getRemark());
        department.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        
        departmentMapper.insert(department);
        return department;
    }
    
    @Transactional
    public void updateDepartment(Long id, DepartmentRequest request) {
        Department existing = departmentMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("部门不存在");
        }
        
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new RuntimeException("不能将自己设为上级部门");
            }
            Department parent = departmentMapper.selectById(request.getParentId());
            if (parent == null) {
                throw new RuntimeException("上级部门不存在");
            }
            if (isDescendant(id, request.getParentId())) {
                throw new RuntimeException("不能将部门移动到其子部门下");
            }
        }
        
        if (request.getManagerId() != null) {
            Employee manager = employeeMapper.selectById(request.getManagerId());
            if (manager == null || manager.getStatus() != 1) {
                throw new RuntimeException("责任人必须是在职员工");
            }
        }
        
        existing.setName(request.getName());
        existing.setParentId(request.getParentId());
        existing.setManagerId(request.getManagerId());
        existing.setRemark(request.getRemark());
        if (request.getSortOrder() != null) {
            existing.setSortOrder(request.getSortOrder());
        }
        
        departmentMapper.updateById(existing);
    }
    
    private boolean isDescendant(Long ancestorId, Long descendantId) {
        Department current = departmentMapper.selectById(descendantId);
        while (current != null && current.getParentId() != null) {
            if (current.getParentId().equals(ancestorId)) {
                return true;
            }
            current = departmentMapper.selectById(current.getParentId());
        }
        return false;
    }
    
    @Transactional
    public void deleteDepartment(Long id) {
        Department existing = departmentMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("部门不存在");
        }
        
        if (departmentMapper.countChildren(id) > 0) {
            throw new RuntimeException("该部门存在下属部门，无法删除，请先删除下属部门");
        }
        
        int memberCount = departmentMapper.countDirectMembers(id);
        if (memberCount > 0) {
            throw new RuntimeException("该部门存在" + memberCount + "名员工，请先调配员工后再删除");
        }
        
        departmentMapper.deleteById(id);
    }
    
    public List<Department> getFlatDepartmentList() {
        List<Department> departments = departmentMapper.selectList(
            new LambdaQueryWrapper<Department>().orderByAsc(Department::getSortOrder).orderByAsc(Department::getId)
        );
        for (Department dept : departments) {
            if (dept.getManagerId() != null) {
                Employee manager = employeeMapper.selectById(dept.getManagerId());
                if (manager != null) {
                    dept.setManagerName(manager.getName());
                }
            }
        }
        return departments;
    }
    
    public List<Long> getAllDescendantIds(Long departmentId) {
        List<Long> ids = new ArrayList<>();
        ids.add(departmentId);
        collectDescendantIds(departmentId, ids);
        return ids;
    }
    
    private void collectDescendantIds(Long parentId, List<Long> ids) {
        List<Department> children = departmentMapper.selectByParentId(parentId);
        for (Department child : children) {
            ids.add(child.getId());
            collectDescendantIds(child.getId(), ids);
        }
    }
}
