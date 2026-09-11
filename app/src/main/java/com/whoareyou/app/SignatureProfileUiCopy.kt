package com.whoareyou.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun localizedSignatureProfileCopy(key: SignatureProfileKey): SignatureProfileCopy = when (key) {
    SignatureProfileKey.INDEPENDENT_EXPLORER -> SignatureProfileCopy(
        stringResource(R.string.signature_independent_explorer_title),
        stringResource(R.string.signature_independent_explorer_body)
    )
    SignatureProfileKey.STRUCTURED_BUILDER -> SignatureProfileCopy(
        stringResource(R.string.signature_structured_builder_title),
        stringResource(R.string.signature_structured_builder_body)
    )
    SignatureProfileKey.OPEN_CONNECTOR -> SignatureProfileCopy(
        stringResource(R.string.signature_open_connector_title),
        stringResource(R.string.signature_open_connector_body)
    )
    SignatureProfileKey.ADAPTIVE_DIPLOMAT -> SignatureProfileCopy(
        stringResource(R.string.signature_adaptive_diplomat_title),
        stringResource(R.string.signature_adaptive_diplomat_body)
    )
    SignatureProfileKey.DRIVEN_CHALLENGER -> SignatureProfileCopy(
        stringResource(R.string.signature_driven_challenger_title),
        stringResource(R.string.signature_driven_challenger_body)
    )
    SignatureProfileKey.CALM_STRATEGIST -> SignatureProfileCopy(
        stringResource(R.string.signature_calm_strategist_title),
        stringResource(R.string.signature_calm_strategist_body)
    )
}
