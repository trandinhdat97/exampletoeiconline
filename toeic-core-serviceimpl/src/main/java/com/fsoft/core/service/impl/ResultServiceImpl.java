package vn.myclass.core.service.impl;

import org.apache.commons.lang.StringUtils;
import com.fsoft.core.dao.ExaminationDao;
import com.fsoft.core.dao.ResultDao;
import com.fsoft.core.dao.UserDao;
import com.fsoft.core.daoimpl.ExaminationDaoImpl;
import com.fsoft.core.daoimpl.ResultDaoImpl;
import com.fsoft.core.daoimpl.UserDaoImpl;
import com.fsoft.core.dto.ExaminationQuestionDTO;
import com.fsoft.core.dto.ResultDTO;
import com.fsoft.core.persistence.entity.ExaminationEntity;
import com.fsoft.core.persistence.entity.ResultEntity;
import com.fsoft.core.persistence.entity.UserEntity;
import vn.myclass.core.service.ResultService;
import vn.myclass.core.service.utils.SingletonDaoUtil;
import vn.myclass.core.utils.ResultBeanUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Admin on 3/12/2017.
 */
public class ResultServiceImpl implements ResultService {

	private UserDao userDao;
	private ResultDao resultDao;
	private ExaminationDao examinationDao;

	public ResultServiceImpl() {
		userDao = new UserDaoImpl();
		resultDao = new ResultDaoImpl();
		examinationDao = new ExaminationDaoImpl();
	}

	public ResultDTO saveResult(String userName, Integer examinationId, List<ExaminationQuestionDTO> examinationQuestions) {
		ResultDTO result = new ResultDTO();
		if (userName != null && examinationId != null && examinationQuestions != null) {
			UserEntity user = SingletonDaoUtil.getUserDaoInstance().findEqualUnique("name", userName);
			ExaminationEntity examination = SingletonDaoUtil.getExaminationDaoInstance().findById(examinationId);
			ResultEntity resultEntity = new ResultEntity();
			calculateListenAndReadScore(resultEntity, examinationQuestions);
			initDataToResultEntity(resultEntity, user, examination);
			resultEntity = SingletonDaoUtil.getResultDaoInstance().save(resultEntity);
			result = ResultBeanUtil.entity2Dto(resultEntity);
		}
		return result;
	}

	public List<ResultDTO> getResultsByUser(String userName, String examinationCode) {
		List<ResultDTO> resultDTOS = new ArrayList<>();
		UserEntity userEntity = userDao.findEqualUnique("name", userName);
		List<ResultEntity> resultEntities = new ArrayList<>();
		if (examinationCode != null && StringUtils.isNotEmpty(examinationCode)) {
			ExaminationEntity examinationEntity = examinationDao.findEqualUnique("code", examinationCode);
			Map<String, Object> properties = new HashMap<>();
			properties.put("user", userEntity);
			properties.put("examination", examinationEntity);
			resultEntities = resultDao.findByProperty(properties);
		} else {
			resultEntities = resultDao.findByProperty("user", userEntity);
		}
		resultEntities.forEach(item -> {
			ResultDTO resultDTO = ResultBeanUtil.entity2Dto(item);
			resultDTOS.add(resultDTO);
		});
		return resultDTOS;
	}

	private void initDataToResultEntity(ResultEntity resultEntity, UserEntity user, ExaminationEntity examination) {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		resultEntity.setExamination(examination);
		resultEntity.setUser(user);
		resultEntity.setCreatedDate(timestamp);
	}

	private void calculateListenAndReadScore(ResultEntity resultEntity, List<ExaminationQuestionDTO> examinationQuestions) {
		int listenScore = 0;
		int readingScore = 0;
		for (ExaminationQuestionDTO item: examinationQuestions) {
			if (item.getAnswerUser() != null) {
				if (item.getAnswerUser().equals(item.getCorrectAnswer())) {
					if (item.getNumber() <= 4) {
						listenScore++;
					} else {
						readingScore++;
					}
				}
			}
		}
		resultEntity.setListenScore(listenScore);
		resultEntity.setReadingScore(readingScore);
	}
}