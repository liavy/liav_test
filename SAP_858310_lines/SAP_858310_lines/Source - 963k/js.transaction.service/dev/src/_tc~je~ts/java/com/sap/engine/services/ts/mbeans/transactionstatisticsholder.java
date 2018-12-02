package com.sap.engine.services.ts.mbeans;

class TransactionStatisticsHolder {
	/*
	 * averageCommitTime = totalCommitTime / totalCommittedTxWithCommitTime
	 * totalCommittedTxWithCommitTime = totalCommittedTxPerNode1 + ... +
	 * totalCommittedTxN
	 * totalCommitTime = totalCommitTimePerNode1 + ... +
	 * totalCommitTimePerNodeN
	 * totalCommitTimePerNodeX =
	 * averageCommitTimePerNodeX * totalCommittedTxPerNodeX
	 */
	private long totalCommitTime = -1;
	private long totalCommittedTxWithCommitTime = -1;

	private long activeTxCount = -1;
	private long timeoutedTxCount = -1;
	private long totalCommittedTx = -1;
	private long totalCompletedTx = -1;
	private long totalRollbackedTx = -1;
	private long suspendedTxCount = -1;
	private long txPassedAbandonTimeout = -1;
	private long txRollbackedBecauseRMError = -1;
	private long txRollbackedByApplication = -1;
	private long txWithHeuristicOutcomesCount = -1;
	private long pendingTxCount = -1;
	private long notRecoveredTxCount = -1;

	private SAP_ITSAMJ2eeActionStatus actionStatus;

	TransactionStatisticsHolder() {
		actionStatus = new SAP_ITSAMJ2eeActionStatus();
		actionStatus.setCode(SAP_ITSAMJ2eeActionStatus.OK_CODE);
	}

	TransactionStatisticsHolder(SAP_ITSAMTransactionStatisticsData statistics) {
		if (null != statistics.getActionStatus() ) {
			actionStatus = statistics.getActionStatus();
		} else {
			actionStatus = new SAP_ITSAMJ2eeActionStatus();
			actionStatus.setCode(SAP_ITSAMJ2eeActionStatus.OK_CODE);
		}

		activeTxCount = statistics.getActiveTxCount();
		timeoutedTxCount = statistics.getTimeoutedTxCount();
		totalCommittedTx = statistics.getTotalCommittedTx();
		totalCompletedTx = statistics.getTotalCompletedTx();
		totalRollbackedTx = statistics.getTotalRollbackedTx();
		suspendedTxCount = statistics.getSuspendedTxCount();
		txPassedAbandonTimeout = statistics.getTxPassedAbandonTimeout();
		txRollbackedBecauseRMError = statistics.getTxRollbackedBecauseRMError();
		txRollbackedByApplication = statistics.getTxRollbackedByApplication();
		txWithHeuristicOutcomesCount = statistics
				.getTxWithHeuristicOutcomesCount();

		pendingTxCount = statistics.getPendingTxCount();
		notRecoveredTxCount = statistics.getNotRecoveredTxCount();

		if (statistics.getAverageCommitTime() >= 0
				&& statistics.getTotalCommittedTx() >= 0) {
			// if averageCommitTime and totalCommittedTx are both valid
			// then we can calculate the totalCommitTime
			totalCommitTime = ((long) statistics.getAverageCommitTime() * statistics
					.getTotalCommittedTx());
			totalCommittedTxWithCommitTime = statistics.getTotalCommittedTx();
		} else {
			// if either averageCommitTime or totalCommittedTx are invalid
			// then we cannot calculate totalCommitTime and we mark is as
			// invalid
			totalCommitTime = -1;
			totalCommittedTxWithCommitTime = -1;
		}
	}

	SAP_ITSAMJ2eeActionStatus getActionStatus() {
		return this.actionStatus;
	}

	void setActionStatus(String code, String messageId, String[] parameters,
			String stackTrace) {
		actionStatus.setCode(code);
		actionStatus.setMessageId(messageId);
		actionStatus.setMessageParameters(parameters);
		actionStatus.setStackTrace(stackTrace);
	}

	void addTransactionStatistics(SAP_ITSAMTransactionStatisticsData statistics) {
		if (null != statistics.getActionStatus()) {
			if (SAP_ITSAMJ2eeActionStatus.ERROR_CODE.equals(statistics
					.getActionStatus().getCode())) {
				actionStatus = statistics.getActionStatus();
				return;
			}

			if (SAP_ITSAMJ2eeActionStatus.WARNING_CODE.equals(statistics
							.getActionStatus().getCode())
					&& !SAP_ITSAMJ2eeActionStatus.ERROR_CODE.equals(actionStatus
							.getCode())) {
				actionStatus = statistics.getActionStatus();
			}
		}

		activeTxCount = sumTxStatistics(activeTxCount, statistics
				.getActiveTxCount());

		timeoutedTxCount = sumTxStatistics(timeoutedTxCount, statistics
				.getTimeoutedTxCount());

		totalCommittedTx = sumTxStatistics(totalCommittedTx, statistics
				.getTotalCommittedTx());

		totalCompletedTx = sumTxStatistics(totalCompletedTx, statistics
				.getTotalCompletedTx());

		totalRollbackedTx = sumTxStatistics(totalRollbackedTx, statistics
				.getTotalRollbackedTx());

		suspendedTxCount = sumTxStatistics(suspendedTxCount, statistics
				.getSuspendedTxCount());

		txPassedAbandonTimeout = sumTxStatistics(txPassedAbandonTimeout,
				statistics.getTxPassedAbandonTimeout());

		txRollbackedBecauseRMError = sumTxStatistics(
				txRollbackedBecauseRMError, statistics
						.getTxRollbackedBecauseRMError());

		txRollbackedByApplication = sumTxStatistics(txRollbackedByApplication,
				statistics.getTxRollbackedByApplication());

		txWithHeuristicOutcomesCount = sumTxStatistics(
				txWithHeuristicOutcomesCount, statistics
						.getTxWithHeuristicOutcomesCount());

		pendingTxCount = sumTxStatistics(pendingTxCount, statistics
				.getPendingTxCount());

		notRecoveredTxCount = sumTxStatistics(notRecoveredTxCount, statistics
				.getNotRecoveredTxCount());

		if (statistics.getAverageCommitTime() >= 0
				&& statistics.getTotalCommittedTx() >= 0) {
			// if averageCommitTime and totalCommittedTx are both valid
			// then we can calculate the tctToAdd
			long tctToAdd = ((long) statistics.getAverageCommitTime() * statistics
					.getTotalCommittedTx());
			totalCommitTime = sumTxStatistics(totalCommitTime, tctToAdd);
			totalCommittedTxWithCommitTime = sumTxStatistics(
					totalCommittedTxWithCommitTime, statistics.getTotalCommittedTx());
		}
		// if either averageCommitTime or totalCommittedTx are invalid
		// then we cannot calculate tctToAdd and and do nothing
	}

	private static long sumTxStatistics(long oldStat, long newStat) {
		if (newStat >= 0 && oldStat >= 0) {
			// if both statistics are valid then the result is their sum
			return oldStat + newStat;
		} else if (newStat >= 0) {
			// if the newStat is valid then the oldStat is invalid and
			// the result is the valid statistics (newStat)
			return newStat;
		} else {
			// if newStat is invalid then oldStat is either valid or
			// invalid. If it's valid we take it. If it's invalid
			// then both statistics are invalid and we must return
			// invalid value - no matter which one
			return oldStat;
		}
	}

	SAP_ITSAMTransactionStatisticsData toSAP_ITSAMTransactionStatisticsData(
			String name, String caption, String description, String elementName) {

		int averageCommitTime;
		if (totalCommitTime >= 0 && totalCommittedTxWithCommitTime >= 0) {
			if (totalCommittedTxWithCommitTime==0) {
				averageCommitTime = 0;
			} else {
				// if totalCommitTime and totalCommittedTx have valid
				// values then we can calculate the averageCommitTime
				averageCommitTime = (int) (totalCommitTime / totalCommittedTxWithCommitTime);
			}
		} else {
			// else we mark it as invalid
			averageCommitTime = -1;
		}

		SAP_ITSAMTransactionStatisticsData res = new SAP_ITSAMTransactionStatisticsData(
				name, pendingTxCount, totalCompletedTx, totalCommittedTx,
				totalRollbackedTx, activeTxCount, suspendedTxCount,
				txWithHeuristicOutcomesCount, txPassedAbandonTimeout,
				txRollbackedByApplication, txRollbackedBecauseRMError,
				timeoutedTxCount, averageCommitTime, notRecoveredTxCount,
				caption, description, elementName);
		res.setActionStatus(actionStatus);
		return res;
	}
}
