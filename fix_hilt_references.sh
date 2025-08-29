#!/bin/bash

# Fix Hilt references in all files
find artiusid-sdk -name "*.kt" -exec sed -i '' 's/import androidx\.hilt\.navigation\.compose\.hiltViewModel/import androidx.lifecycle.viewmodel.compose.viewModel/g' {} \;
find artiusid-sdk -name "*.kt" -exec sed -i '' 's/import dagger\.hilt\.android\.lifecycle\.HiltViewModel//g' {} \;
find artiusid-sdk -name "*.kt" -exec sed -i '' 's/import javax\.inject\.Inject//g' {} \;
find artiusid-sdk -name "*.kt" -exec sed -i '' 's/@HiltViewModel//g' {} \;
find artiusid-sdk -name "*.kt" -exec sed -i '' 's/hiltViewModel()/viewModel()/g' {} \;
find artiusid-sdk -name "*.kt" -exec sed -i '' 's/@Inject constructor(/constructor(/g' {} \;
find artiusid-sdk -name "*.kt" -exec sed -i '' 's/@ApplicationContext//g' {} \;

echo "Hilt references fixed!"
