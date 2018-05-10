package vn.myclass.controller.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import vn.myclass.command.ExerciseQuestionCommand;
import com.fsoft.core.dto.ExerciseQuestionDTO;
import vn.myclass.core.web.common.WebConstant;
import vn.myclass.core.web.utils.FormUtil;
import vn.myclass.core.web.utils.RequestUtil;
import vn.myclass.core.web.utils.SingletonServiceUtil;

@WebServlet(urlPatterns = {"/bai-tap-thuc-hanh.html","/ajax-bai-tap-dap-an.html"})
public class ExerciseQuestionController extends HttpServlet {
	
	private static final long serialVersionUID = 3575223830998239809L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ExerciseQuestionCommand command = FormUtil.populate(ExerciseQuestionCommand.class, request);
		getListenExerciseQuestion(command);
		request.setAttribute(WebConstant.LIST_ITEMS, command);
		if (request.getParameter("message") != null && request.getParameter("message").equals("confirm-point-success")) {
			request.setAttribute("alert", "success");
			request.setAttribute("messageResponse", "Xác nhận chấm điểm thành công, nhấn Next để làm tiếp");
		} else if (request.getParameter("message") != null && request.getParameter("message").equals("check-point-success")) {
			request.setAttribute("alert", "success");
			request.setAttribute("messageResponse", "Chấm điểm bài tập thành công. Truy cập vào mục kết quả bài tập để xem kết quả");
		}
		RequestDispatcher rd = request.getRequestDispatcher("/views/web/exercise/detail.jsp");
		rd.forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ExerciseQuestionCommand command = FormUtil.populate(ExerciseQuestionCommand.class, request);
		getListenExerciseQuestion(command);
		for (ExerciseQuestionDTO item: command.getListResult()) {
			if (!command.getAnswerUser().equals(item.getCorrectAnswer())) {
				command.setCheckAnswer(true);
			}
		}
		request.setAttribute(WebConstant.LIST_ITEMS, command);
		RequestDispatcher rd = request.getRequestDispatcher("/views/web/exercise/result.jsp");
		rd.forward(request, response);
	}

	private void getListenExerciseQuestion(ExerciseQuestionCommand command) {
		command.setMaxPageItems(1);
		RequestUtil.initSearchBeanManual(command);
		Object[] objects = SingletonServiceUtil.getExerciseQuestionServiceInstance().findExerciseQuestionByProperties(new HashMap<String, Object>(), command.getSortExpression(),
				command.getSortDirection(), command.getFirstItem(), command.getMaxPageItems(), command.getExerciseId());
		command.setListResult((List<ExerciseQuestionDTO>) objects[1]);
		command.setTotalItems(Integer.parseInt(objects[0].toString()));
		command.setTotalPages((int) Math.ceil((double) command.getTotalItems() / command.getMaxPageItems()));
	}
}