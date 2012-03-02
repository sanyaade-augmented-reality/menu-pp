var NAVTREE =
[
  [ "Menu++", "index.html", [
    [ "Class List", "annotated.html", [
      [ "srdes.menupp.AboutUs", "classsrdes_1_1menupp_1_1_about_us.html", null ],
      [ "srdes.menupp.DebugLog", "classsrdes_1_1menupp_1_1_debug_log.html", null ],
      [ "srdes.menupp.EntreeDbAdapter", "classsrdes_1_1menupp_1_1_entree_db_adapter.html", null ],
      [ "srdes.menupp.EntreeEdit", "classsrdes_1_1menupp_1_1_entree_edit.html", null ],
      [ "srdes.menupp.EntreeTabManage", "classsrdes_1_1menupp_1_1_entree_tab_manage.html", null ],
      [ "EntreeTarget", "class_entree_target.html", null ],
      [ "srdes.menupp.QcarEngine.InitQCARTask", "classsrdes_1_1menupp_1_1_qcar_engine_1_1_init_q_c_a_r_task.html", null ],
      [ "srdes.menupp.QcarEngine.LoadTrackerTask", "classsrdes_1_1menupp_1_1_qcar_engine_1_1_load_tracker_task.html", null ],
      [ "srdes.menupp.MenuList", "classsrdes_1_1menupp_1_1_menu_list.html", null ],
      [ "srdes.menupp.menupp", "classsrdes_1_1menupp_1_1menupp.html", null ],
      [ "srdes.menupp.menuppRenderer", "classsrdes_1_1menupp_1_1menupp_renderer.html", null ],
      [ "srdes.menupp.QcarEngine", "classsrdes_1_1menupp_1_1_qcar_engine.html", null ],
      [ "srdes.menupp.QCARSampleGLView", "classsrdes_1_1menupp_1_1_q_c_a_r_sample_g_l_view.html", null ],
      [ "Texture", "class_texture.html", null ],
      [ "srdes.menupp.Texture", "classsrdes_1_1menupp_1_1_texture.html", null ],
      [ "srdes.menupp.UserGuide", "classsrdes_1_1menupp_1_1_user_guide.html", null ],
      [ "Utils", "class_utils.html", null ],
      [ "srdes.menupp.ViewEntree", "classsrdes_1_1menupp_1_1_view_entree.html", null ],
      [ "srdes.menupp.ViewReview", "classsrdes_1_1menupp_1_1_view_review.html", null ],
      [ "VirtualButton_UpdateCallback", "class_virtual_button___update_callback.html", null ]
    ] ],
    [ "Class Index", "classes.html", null ],
    [ "Class Members", "functions.html", null ],
    [ "Packages", "namespaces.html", [
      [ "srdes", "namespacesrdes.html", null ],
      [ "srdes.menupp", "namespacesrdes_1_1menupp.html", null ]
    ] ],
    [ "File List", "files.html", [
      [ "jni/CubeShaders.h", "_cube_shaders_8h.html", null ],
      [ "jni/EntreeTarget.cpp", "_entree_target_8cpp.html", null ],
      [ "jni/EntreeTarget.h", "_entree_target_8h.html", null ],
      [ "jni/menupp.cpp", "menupp_8cpp.html", null ],
      [ "jni/Menupp.h", "_menupp_8h.html", null ],
      [ "jni/Planes.h", "_planes_8h.html", null ],
      [ "jni/Texture.cpp", "_texture_8cpp.html", null ],
      [ "jni/Texture.h", "_texture_8h.html", null ],
      [ "jni/Utils.cpp", "_utils_8cpp.html", null ],
      [ "jni/Utils.h", "_utils_8h.html", null ],
      [ "src/srdes/menupp/AboutUs.java", "_about_us_8java.html", null ],
      [ "src/srdes/menupp/DebugLog.java", "_debug_log_8java.html", null ],
      [ "src/srdes/menupp/EntreeDbAdapter.java", "_entree_db_adapter_8java.html", null ],
      [ "src/srdes/menupp/EntreeEdit.java", "_entree_edit_8java.html", null ],
      [ "src/srdes/menupp/EntreeTabManage.java", "_entree_tab_manage_8java.html", null ],
      [ "src/srdes/menupp/MenuList.java", "_menu_list_8java.html", null ],
      [ "src/srdes/menupp/menupp.java", "menupp_8java.html", null ],
      [ "src/srdes/menupp/menuppRenderer.java", "menupp_renderer_8java.html", null ],
      [ "src/srdes/menupp/QcarEngine.java", "_qcar_engine_8java.html", null ],
      [ "src/srdes/menupp/QCARSampleGLView.java", "_q_c_a_r_sample_g_l_view_8java.html", null ],
      [ "src/srdes/menupp/Texture.java", "_texture_8java.html", null ],
      [ "src/srdes/menupp/UserGuide.java", "_user_guide_8java.html", null ],
      [ "src/srdes/menupp/ViewEntree.java", "_view_entree_8java.html", null ],
      [ "src/srdes/menupp/ViewReview.java", "_view_review_8java.html", null ]
    ] ],
    [ "File Members", "globals.html", null ]
  ] ]
];

function createIndent(o,domNode,node,level)
{
  if (node.parentNode && node.parentNode.parentNode)
  {
    createIndent(o,domNode,node.parentNode,level+1);
  }
  var imgNode = document.createElement("img");
  if (level==0 && node.childrenData)
  {
    node.plus_img = imgNode;
    node.expandToggle = document.createElement("a");
    node.expandToggle.href = "javascript:void(0)";
    node.expandToggle.onclick = function() 
    {
      if (node.expanded) 
      {
        $(node.getChildrenUL()).slideUp("fast");
        if (node.isLast)
        {
          node.plus_img.src = node.relpath+"ftv2plastnode.png";
        }
        else
        {
          node.plus_img.src = node.relpath+"ftv2pnode.png";
        }
        node.expanded = false;
      } 
      else 
      {
        expandNode(o, node, false);
      }
    }
    node.expandToggle.appendChild(imgNode);
    domNode.appendChild(node.expandToggle);
  }
  else
  {
    domNode.appendChild(imgNode);
  }
  if (level==0)
  {
    if (node.isLast)
    {
      if (node.childrenData)
      {
        imgNode.src = node.relpath+"ftv2plastnode.png";
      }
      else
      {
        imgNode.src = node.relpath+"ftv2lastnode.png";
        domNode.appendChild(imgNode);
      }
    }
    else
    {
      if (node.childrenData)
      {
        imgNode.src = node.relpath+"ftv2pnode.png";
      }
      else
      {
        imgNode.src = node.relpath+"ftv2node.png";
        domNode.appendChild(imgNode);
      }
    }
  }
  else
  {
    if (node.isLast)
    {
      imgNode.src = node.relpath+"ftv2blank.png";
    }
    else
    {
      imgNode.src = node.relpath+"ftv2vertline.png";
    }
  }
  imgNode.border = "0";
}

function newNode(o, po, text, link, childrenData, lastNode)
{
  var node = new Object();
  node.children = Array();
  node.childrenData = childrenData;
  node.depth = po.depth + 1;
  node.relpath = po.relpath;
  node.isLast = lastNode;

  node.li = document.createElement("li");
  po.getChildrenUL().appendChild(node.li);
  node.parentNode = po;

  node.itemDiv = document.createElement("div");
  node.itemDiv.className = "item";

  node.labelSpan = document.createElement("span");
  node.labelSpan.className = "label";

  createIndent(o,node.itemDiv,node,0);
  node.itemDiv.appendChild(node.labelSpan);
  node.li.appendChild(node.itemDiv);

  var a = document.createElement("a");
  node.labelSpan.appendChild(a);
  node.label = document.createTextNode(text);
  a.appendChild(node.label);
  if (link) 
  {
    a.href = node.relpath+link;
  } 
  else 
  {
    if (childrenData != null) 
    {
      a.className = "nolink";
      a.href = "javascript:void(0)";
      a.onclick = node.expandToggle.onclick;
      node.expanded = false;
    }
  }

  node.childrenUL = null;
  node.getChildrenUL = function() 
  {
    if (!node.childrenUL) 
    {
      node.childrenUL = document.createElement("ul");
      node.childrenUL.className = "children_ul";
      node.childrenUL.style.display = "none";
      node.li.appendChild(node.childrenUL);
    }
    return node.childrenUL;
  };

  return node;
}

function showRoot()
{
  var headerHeight = $("#top").height();
  var footerHeight = $("#nav-path").height();
  var windowHeight = $(window).height() - headerHeight - footerHeight;
  navtree.scrollTo('#selected',0,{offset:-windowHeight/2});
}

function expandNode(o, node, imm)
{
  if (node.childrenData && !node.expanded) 
  {
    if (!node.childrenVisited) 
    {
      getNode(o, node);
    }
    if (imm)
    {
      $(node.getChildrenUL()).show();
    } 
    else 
    {
      $(node.getChildrenUL()).slideDown("fast",showRoot);
    }
    if (node.isLast)
    {
      node.plus_img.src = node.relpath+"ftv2mlastnode.png";
    }
    else
    {
      node.plus_img.src = node.relpath+"ftv2mnode.png";
    }
    node.expanded = true;
  }
}

function getNode(o, po)
{
  po.childrenVisited = true;
  var l = po.childrenData.length-1;
  for (var i in po.childrenData) 
  {
    var nodeData = po.childrenData[i];
    po.children[i] = newNode(o, po, nodeData[0], nodeData[1], nodeData[2],
        i==l);
  }
}

function findNavTreePage(url, data)
{
  var nodes = data;
  var result = null;
  for (var i in nodes) 
  {
    var d = nodes[i];
    if (d[1] == url) 
    {
      return new Array(i);
    }
    else if (d[2] != null) // array of children
    {
      result = findNavTreePage(url, d[2]);
      if (result != null) 
      {
        return (new Array(i).concat(result));
      }
    }
  }
  return null;
}

function initNavTree(toroot,relpath)
{
  var o = new Object();
  o.toroot = toroot;
  o.node = new Object();
  o.node.li = document.getElementById("nav-tree-contents");
  o.node.childrenData = NAVTREE;
  o.node.children = new Array();
  o.node.childrenUL = document.createElement("ul");
  o.node.getChildrenUL = function() { return o.node.childrenUL; };
  o.node.li.appendChild(o.node.childrenUL);
  o.node.depth = 0;
  o.node.relpath = relpath;

  getNode(o, o.node);

  o.breadcrumbs = findNavTreePage(toroot, NAVTREE);
  if (o.breadcrumbs == null)
  {
    o.breadcrumbs = findNavTreePage("index.html",NAVTREE);
  }
  if (o.breadcrumbs != null && o.breadcrumbs.length>0)
  {
    var p = o.node;
    for (var i in o.breadcrumbs) 
    {
      var j = o.breadcrumbs[i];
      p = p.children[j];
      expandNode(o,p,true);
    }
    p.itemDiv.className = p.itemDiv.className + " selected";
    p.itemDiv.id = "selected";
    $(window).load(showRoot);
  }
}

