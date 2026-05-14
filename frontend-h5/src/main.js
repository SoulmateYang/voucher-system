import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import {
  Button,
  Form,
  Field,
  Checkbox,
  Toast,
  NavBar,
  PullRefresh,
  List,
  Tag,
  Cell,
  CellGroup,
  Skeleton,
  Empty,
  Tabbar,
  TabbarItem,
  Icon,
  Search,
  Loading,
} from 'vant';
import 'vant/lib/index.css';
import './styles/global.css';

const app = createApp(App);

app.use(router);
app.use(Button);
app.use(Form);
app.use(Field);
app.use(Checkbox);
app.use(Toast);
app.use(NavBar);
app.use(PullRefresh);
app.use(List);
app.use(Tag);
app.use(Cell);
app.use(CellGroup);
app.use(Skeleton);
app.use(Empty);
app.use(Tabbar);
app.use(TabbarItem);
app.use(Icon);
app.use(Search);
app.use(Loading);

app.mount('#app');
