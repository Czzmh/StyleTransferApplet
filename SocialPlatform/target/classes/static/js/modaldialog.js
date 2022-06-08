/**
 * 消息提示框（警告框）
 * 重写window的alert()函数
 * @param {Object} message 提示信息
 * @param {Object} title 标题
 */
window.alert = function(message, title) {
	//打开模态框，网页中追加模态框及内部元素的对象
	$('body').append('<div id="modalDialog">'
			+'<div class="modal-dialog">'
				+'<i class="fa fa-remove"></i>'
				+'<p class="modal-dialog-title">'+(title?title:'提示')+'</p>'
				+'<p class="modal-dialog-message">'+message+'</p>'
				+'<p class="modal-dialog-btns">'
					+'<input type="button" value="确 定" class="btn btn-primary btn-sm" />'
				+'</p>'
			+'</div>'
		+'</div>');
	//绑定关闭模态框架的事件
	$('#modalDialog .modal-dialog i.fa-remove').on('click', function() {
		//关闭模态框，删除模态框架元素对象
		$("#modalDialog").remove();
	});
	$('#modalDialog .modal-dialog p.modal-dialog-btns input').on('click', function() {
		$("#modalDialog").remove();
	});
}

/**
 * 选择对话框
 * 重写window的confirm()函数
 * @param {Object} message
 * @param {Object} callback 回调函数，函数的对象
 * @param {Object} title
 */
window.confirm = function(message, callback, title) {
	//打开模态框，网页中追加模态框及内部元素的对象
	$('body').append('<div id="modalDialog">'
			+'<div class="modal-dialog">'
				+'<i class="fa fa-remove"></i>'
				+'<p class="modal-dialog-title">'+(title?title:'提示')+'</p>'
				+'<p class="modal-dialog-message">'+message+'</p>'
				+'<p class="modal-dialog-btns">'
					+'<input type="button" value="取 消" class="btn btn-default btn-sm" />'
					+'<input type="button" value="确 定" class="btn btn-primary btn-sm" />'
				+'</p>'
			+'</div>'
		+'</div>');
	//绑定关闭模态框架的事件
	$('#modalDialog .modal-dialog i.fa-remove').on('click', function() {
		//关闭模态框，删除模态框架元素对象
		$("#modalDialog").remove();
	});
	$('#modalDialog .modal-dialog p.modal-dialog-btns input:first-child').on('click', function() {
		$("#modalDialog").remove();
	});
	$('#modalDialog .modal-dialog p.modal-dialog-btns input:last-child').on('click', function() {
		$("#modalDialog").remove();
		//执行回调函数
		callback.call();
	});
}