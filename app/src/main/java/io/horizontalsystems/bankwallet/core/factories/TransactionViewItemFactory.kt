package io.horizontalsystems.bankwallet.core.factories

import io.horizontalsystems.bankwallet.entities.*
import io.horizontalsystems.bankwallet.modules.transactions.TransactionStatus
import io.horizontalsystems.bankwallet.modules.transactions.TransactionViewItem
import java.util.*

class TransactionViewItemFactory(private val feeCoinProvider: FeeCoinProvider) {

    fun item(wallet: Wallet, transactionItem: TransactionItem, lastBlockHeight: Int?, threshold: Int, rate: CurrencyValue?): TransactionViewItem {
        val record = transactionItem.record

        var status: TransactionStatus = TransactionStatus.Pending

        if (record.failed) {
            status = TransactionStatus.Failed
        } else if (record.blockHeight != null && lastBlockHeight != null) {

            val confirmations = lastBlockHeight - record.blockHeight.toInt() + 1
            if (confirmations >= 0) {
                status = when {
                    confirmations >= threshold -> TransactionStatus.Completed
                    else -> TransactionStatus.Processing(confirmations.toDouble() / threshold.toDouble())
                }
            }
        }

        val currencyValue = rate?.let {
            CurrencyValue(it.currency, record.amount * it.value)
        }

        val coin = transactionItem.wallet.coin
        val date = if (record.timestamp == 0L) null else Date(record.timestamp * 1000)

        val feeCoinValue = transactionItem.record.fee?.let {
            val feeCoin = feeCoinProvider.feeCoinData(coin)?.first ?: coin
            CoinValue(feeCoin, transactionItem.record.fee)
        }

        return TransactionViewItem(
                wallet,
                record.transactionHash,
                CoinValue(coin, record.amount),
                currencyValue,
                feeCoinValue,
                record.from,
                record.to,
                record.type,
                showFromAddress(wallet.coin.type),
                date,
                status,
                rate,
                record.lockInfo
        )
    }

    private fun showFromAddress(coinType: CoinType): Boolean {
        return !(coinType == CoinType.Bitcoin || coinType == CoinType.BitcoinCash || coinType == CoinType.Dash)
    }

}
