#!/bin/bash

# Fix UI component imports
find artiusid-sdk -name "*.kt" -exec sed -i '' 's/import com\.artiusid\.sdk\.presentation\.components\.AppTopBar/import com.artiusid.sdk.ui.components.AppTopBar/g' {} \;
find artiusid-sdk -name "*.kt" -exec sed -i '' 's/import com\.artiusid\.sdk\.ui\.theme\.GradientBackground/import com.artiusid.sdk.ui.components.GradientBackground/g' {} \;
find artiusid-sdk -name "*.kt" -exec sed -i '' 's/import com\.artiusid\.sdk\.presentation\.components\.CustomInfoButton/import com.artiusid.sdk.ui.components.CustomInfoButton/g' {} \;
find artiusid-sdk -name "*.kt" -exec sed -i '' 's/import com\.artiusid\.sdk\.presentation\.components\.LoadingIndicator/import com.artiusid.sdk.ui.components.LoadingIndicator/g' {} \;
find artiusid-sdk -name "*.kt" -exec sed -i '' 's/import com\.artiusid\.sdk\.presentation\.components\./import com.artiusid.sdk.ui.components./g' {} \;

echo "UI component imports fixed!"
