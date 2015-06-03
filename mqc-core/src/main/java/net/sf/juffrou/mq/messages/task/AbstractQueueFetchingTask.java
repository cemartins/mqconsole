package net.sf.juffrou.mq.messages.task;

import java.util.List;

import javafx.concurrent.Task;
import net.sf.juffrou.mq.dom.QueueDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract task used to fetch the existing queues / topics in the message broker before displaying the main window and upon refresh of the main window.<p>
 * Implementation can call the method updateMessage(". . ."); to inform about the progress. 
 * @author Carlos Martins
 *
 */
public abstract class AbstractQueueFetchingTask extends Task<List<QueueDescriptor>> {

	protected static final Logger LOG = LoggerFactory.getLogger(AbstractQueueFetchingTask.class);

	@Override
	protected List<QueueDescriptor> call() throws InterruptedException {
		return fetchQueues();
	}
	
	public abstract List<QueueDescriptor> fetchQueues();
	
}
