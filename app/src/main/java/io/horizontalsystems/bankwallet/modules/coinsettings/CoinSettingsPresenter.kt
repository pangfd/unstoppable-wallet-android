package io.horizontalsystems.bankwallet.modules.coinsettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.App
import io.horizontalsystems.bankwallet.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CoinSettingsPresenter(
        private val coin: Coin,
        private var coinSettings: CoinSettings,
        private val settingsMode: SettingsMode,
        val view: CoinSettingsModule.IView,
        val router: CoinSettingsModule.IRouter
) : ViewModel(), CoinSettingsModule.IViewDelegate {

    override fun viewDidLoad() {
        viewModelScope.launch(Dispatchers.IO) {
            view.setTitle(coin.title)

            val derivationList = mutableListOf<SettingSection>()
            val syncModeList = mutableListOf<SettingSection>()

            for ((key, value) in coinSettings) {
                when (key) {
                    CoinSetting.Derivation -> {
                        val derivation = AccountType.Derivation.valueOf(value)

                        derivationList.add(SettingSection.Header(App.instance.getString(R.string.CoinOption_AddressFormatTitle)))

                        derivationList.addAll(getBips(derivation))

                        when (settingsMode) {
                            SettingsMode.Creating -> derivationList.add(SettingSection.Description(App.instance.getString(R.string.CoinOption_BipDescriptionCreate)))
                            SettingsMode.Restoring -> derivationList.add(SettingSection.Description(App.instance.getString(R.string.CoinOption_BipDescriptionRestore)))
                        }
                    }
                    CoinSetting.SyncMode -> {
                        val syncMode = SyncMode.valueOf(value)

                        syncModeList.add(SettingSection.Header(App.instance.getString(R.string.CoinOption_SyncModeTitle)))

                        syncModeList.addAll(getSyncModes(syncMode))

                        if (settingsMode == SettingsMode.Restoring) {
                            syncModeList.add(SettingSection.Description(App.instance.getString(R.string.CoinOption_SyncModeDescription, coin.title, coin.type.restoreUrl())))
                        }
                    }
                }
            }

            val items = mutableListOf<SettingSection>()
            items.addAll(derivationList)
            items.addAll(syncModeList)

            view.setItems(items.toList())
        }
    }

    override fun onSelect(syncMode: SyncMode) {
        coinSettings[CoinSetting.SyncMode] = syncMode.value
    }

    override fun onSelect(derivation: AccountType.Derivation) {
        coinSettings[CoinSetting.Derivation] = derivation.value
    }

    override fun onDone() {
        router.notifyOptions(coinSettings, coin)
    }

    override fun onCancel() {
        router.onCancelClick()
    }

    private fun getBips(derivation: AccountType.Derivation): List<SettingSection> {
        val bip44 = SettingSection.DerivationItem(
                R.string.CoinOption_bip44,
                R.string.CoinOption_bip44_Subtitle,
                AccountType.Derivation.bip44,
                derivation == AccountType.Derivation.bip44
        )

        val bip49 = SettingSection.DerivationItem(
                R.string.CoinOption_bip49,
                R.string.CoinOption_bip49_Subtitle,
                AccountType.Derivation.bip49,
                derivation == AccountType.Derivation.bip49
        )

        val bip84 = SettingSection.DerivationItem(
                R.string.CoinOption_bip84,
                R.string.CoinOption_bip84_Subtitle,
                AccountType.Derivation.bip84,
                derivation == AccountType.Derivation.bip84
        )

        return listOf(bip44, bip49, bip84)
    }

    private fun getSyncModes(syncMode: SyncMode): List<SettingSection> {
        val fastMode = SettingSection.SyncModeItem(
                App.instance.getString(R.string.CoinOption_Fast),
                R.string.CoinOption_Fast_Subtitle,
                SyncMode.Fast,
                syncMode == SyncMode.Fast
        )

        val slowMode = SettingSection.SyncModeItem(
                App.instance.getString(R.string.CoinOption_Slow, coin.title),
                R.string.CoinOption_Slow_Subtitle,
                SyncMode.Slow,
                syncMode == SyncMode.Slow
        )

        return listOf(fastMode, slowMode)
    }

}
