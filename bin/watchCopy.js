const { watch, copy } = require('cpx');
const { resolve } = require('path');
const packageJson = require('./../package.json');

const readline = require('readline');

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

const PROJECT_DIR = resolve(__dirname, './../');
let TARGET_DIR = process.env.TARGET_DIR;

if (!TARGET_DIR) {
  console.error('Missing TARGET_DIR process env, aborting!');
  console.error('EXAMPLE USAGE: TARGET_DIR=/Users/YOU/Documents/SomeReactApp npm run watchcpx');
  process.exit(1);
}

if (!TARGET_DIR.includes('node_modules')) {
  TARGET_DIR = `${TARGET_DIR}/node_modules/${packageJson.name}`;
}

rl.question(`Watch for changes in '${PROJECT_DIR}' and copy to '${TARGET_DIR}'? (y/n): `, (answer) => {
  if (answer.toLowerCase() === 'y') {
    // flat copy node_modules as we're not watching it
    console.log('Copying node_modules directory...');
    copy(PROJECT_DIR + '/node_modules/**/*.*', TARGET_DIR + '/node_modules', { clean: true }, () => {
      console.log('Copy complete.');
      console.log('Watching for changes in project directory... (excludes node_modules)');
      const watcher = watch(PROJECT_DIR + '/{ios,android,lib}/**/*.*', TARGET_DIR, { verbose: true });
      watcher.on('copy', (e) => {
        // if (!e.srcPath.startsWith('node_modules')) {
        console.log(`Copied ${e.srcPath} to ${e.dstPath}`);
        // }
      });
    });
  } else {
    console.log('Aborting watch.');
    process.exit();
  }
  rl.close();
});

