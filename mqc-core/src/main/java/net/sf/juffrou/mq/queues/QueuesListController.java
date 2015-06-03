package net.sf.juffrou.mq.queues;

import java.util.List;

import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.messages.task.AbstractQueueFetchingTask;
import net.sf.juffrou.mq.queues.presenter.QueuesListPresenter;

public interface QueuesListController {

	List<QueueDescriptor> getQueues(QueuesListPresenter presenter);
	
	AbstractQueueFetchingTask getQueueFetchingTask(QueuesListPresenter presenter);
}
