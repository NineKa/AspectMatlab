aspect callGraphWithTiming

patterns
	calls : call(*);
end

actions
	
	callHandle : around calls : (name)
		timer = tic;
		proceed();
		elipsedTime = toc(timer);
		disp([name, num2str(elipsedTime)]);
	end

	

end

end