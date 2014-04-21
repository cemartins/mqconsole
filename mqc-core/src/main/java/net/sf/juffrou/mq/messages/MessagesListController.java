package net.sf.juffrou.mq.messages;

import java.util.List;

import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.messages.presenter.MessagesListPresenter;

public interface MessagesListController {

	List<MessageDescriptor> listMessages(MessagesListPresenter presenter, String queueName);
}
